
/* 
    Large Integer implentation using byte arrays
    Some methods/ideas taken from CS1501 project 5 implentation of HeftyInteger
*/
import java.math.BigInteger;
import java.util.Date;

public class LargeInt {
    private final byte[] ZERO = { (byte) 0 };
    private final byte[] ONE = { (byte) 1 };
    private final byte[] TWO = { (byte) 2 };

    private byte[] value;

    // Constructor from given byte array
    public LargeInt(byte[] b) {
        this.value = b;
    }

    public byte[] getValue() {
        return this.value;
    }

    public byte getByte(int i) {
        return this.value[i];
    }

    public int getLength() {
        return this.value.length;
    }

    public boolean isNegative() {
        //System.out.println("IS NEGATIVE DEBUG = " + new BigInteger(this.value));
        return (this.value[0] < 0);
    }

    public boolean isZero() {
        boolean zero = true;
        for (int i = 0; i < this.getLength(); i++) {
            if (this.getByte(i) != 0) {
                zero = false;
                break;
            }
        }
        return zero;
    }

    //Add a new byte as the most significant in this @param extension the byte to place as most significant
    public void extend(byte extension) {
        byte[] newv = new byte[this.getLength() + 1];
        newv[0] = extension;
        for (int i = 0; i < this.getLength(); i++) {
            newv[i + 1] = this.value[i];
        }
        this.value = newv;
    }

    //Negate 2's complement
    public LargeInt negate() {
        byte[] neg = new byte[this.getLength()];
        int offset = 0;
        // Check to ensure we can represent negation in same length
        //  (e.g., -128 can be represented in 8 bits using two's
        //  complement, +128 requires 9)
        if (this.getValue()[0] == (byte) 0x80) { // 0x80 is 10000000
            boolean needs_ex = true;
            for (int i = 1; i < this.getLength(); i++) {
                if (this.getValue()[i] != (byte) 0) {
                    needs_ex = false;
                    break;
                }
            }
            // if first byte is 0x80 and all others are 0, must extend
            if (needs_ex) {
                neg = new byte[this.getLength() + 1];
                neg[0] = (byte) 0;
                offset = 1;
            }
        }
        // flip all bits
        for (int i = 0; i < this.getLength(); i++) {
            neg[i + offset] = (byte) ~this.getValue()[i];
        }
        LargeInt neg_li = new LargeInt(neg);
        // add 1 to complete two's complement negation
        return neg_li.add(new LargeInt(ONE));
    }

    // Computes addition
    public LargeInt add(LargeInt other) {
        // If different sizes, place larger first
        byte[] a, b;
        if (this.getLength() < other.getLength()) {
            a = other.getValue();
            b = this.getValue();
        } else {
            a = this.getValue();
            b = other.getValue();
        }

        // Normalize size
        if (b.length < a.length) {
            int diff = a.length - b.length;
            byte pad = (byte) 0;
            if (b[0] < 0) {
                pad = (byte) 0xFF;
            }
            byte[] newb = new byte[a.length];
            for (int i = 0; i < diff; i++) {
                newb[i] = pad;
            }
            for (int i = 0; i < b.length; i++) {
                newb[i + diff] = b[i];
            }
            b = newb;
        }

        // Actually compute the add
        int carry = 0;
        byte[] res = new byte[a.length];
        for (int i = a.length - 1; i >= 0; i--) {
            // Be sure to bitmask so that cast of negative bytes does not introduce spurious 1 bits into result of cast
            carry = ((int) a[i] & 0xFF) + ((int) b[i] & 0xFF) + carry;
            // Assign to next byte
            res[i] = (byte) (carry & 0xFF);
            // Carry remainder over to next byte (always want to shift in 0s)
            carry = carry >>> 8;
        }

        LargeInt addition = new LargeInt(res);
        // If both operands are positive, magnitude could increase as a result of addition
        if (!this.isNegative() && !other.isNegative()) {
            // If we have either a leftover carry value or we used the last bit in the most significant byte, we need to extend the result
            if (addition.isNegative()) {
                addition.extend((byte) carry);
            }
        }
        // Magnitude could also increase if both operands are negative
        else if (this.isNegative() && other.isNegative()) {
            if (!addition.isNegative()) {
                addition.extend((byte) 0xFF);
            }
        }
        // Note that result will always be the same size as biggest input
        // (e.g., -127 + 128 will use 2 bytes to store the result value 1)
        return addition;
    }

    // Computes subtraction using negation and addition
    public LargeInt subtract(LargeInt other) {
        return this.add(other.negate());
    }

    // Gradeschool multiplication algorithm
    public LargeInt gradeschool(LargeInt other) {
        LargeInt firstLarge, secondLarge, product;
        // If different sizes, place larger first
        byte[] a, b;
        if (this.getLength() < other.getLength()) {
            a = other.getValue();
            b = this.getValue();
        } else {
            a = this.getValue();
            b = other.getValue();
        }

        // Normalize size
        if (b.length < a.length) {
            int diff = a.length - b.length;
            byte pad = (byte) 0;
            if (b[0] < 0) {
                pad = (byte) 0xFF;
            }
            byte[] newb = new byte[a.length];
            for (int i = 0; i < diff; i++) {
                newb[i] = pad;
            }
            for (int i = 0; i < b.length; i++) {
                newb[i + diff] = b[i];
            }
            b = newb;
        }
        firstLarge = new LargeInt(a);
        secondLarge = new LargeInt(b);
        //Before multiplication, make both numbers positive and remember if we need to negate
        boolean firstNegative = false, secondNegative = false;
        if (firstLarge.isNegative()) {
            firstNegative = true;
            firstLarge = firstLarge.negate();
        }
        if (secondLarge.isNegative()) {
            secondNegative = true;
            secondLarge = secondLarge.negate();
        }

        product = new LargeInt(new byte[firstLarge.getLength() * 2]);
        byte[] temp;
        int firstCurr;
        int secondCurr;

        //Actual gradeschool algorithm using partial products and remainder
        for (int i = 0; i < firstLarge.getLength(); i++) {
            for (int j = 0; j < secondLarge.getLength(); j++) {
                //Creating partial product storage
                temp = new byte[firstLarge.getLength() * 2];
                //Retrieving the current bytes currently calculating the partial product
                firstCurr = firstLarge.getByte(i);
                secondCurr = secondLarge.getByte(j);

                //Only work with positive numbers for mult
                //Adding 256 will get the correct 2s complement representation of negative bytes (-128 -> 127)
                if (firstCurr < 0) {
                    firstCurr += 256;
                }
                if (secondCurr < 0) {
                    secondCurr += 256;
                }
                //computing the partial product
                int mult = (firstCurr * secondCurr);
                //deciding where to put the partial product and the remainder (if there is one)
                int location = i + j + 1;
                temp[location] = (byte) (mult & 0xFF);
                location -= 1;
                //shifting right to include only the remainder portion of the product (overflow byte)
                temp[location] = (byte) ((mult >>> 8) & 0xFF);
                //add the partial product to the product
                product = product.add(new LargeInt(temp));
            }
        }

        //Trim excess leading 0's that were not used (due to the nature of how the product is initialized, it is assumed the entire space will be consumed)
        //Count the amount of leading 0s until you reach the Most Significant Byte
        //This is a similar process to the trimLeadingZeros() method, but if there is an unknown length that the result will end up, this will accomadate rather than rely on hardcoded length value
        int msb = 0;
        while (product.getValue()[msb] == 0) {
            msb++;
        }
        //Make sure to leave one zero at the front (avoid turning a positive number to a negative one)
        msb--;

        //If there are leading 0s
        if (msb > 0) {
            //Actual trimming, new length with be the length of original - position of first most significant bit that is not 0
            byte[] replace = new byte[product.getLength() - msb];
            for (int i = 0; i < replace.length; i++) {
                replace[i] = product.getByte(msb + i);
            }
            product = new LargeInt(replace);
        }

        //If either number was initially negative, we make the product negative
        //do not make product negative if both were negative
        if ((firstNegative == false && secondNegative == true) || (firstNegative == true && secondNegative == false)) {
            product = product.negate();
        }

        return product;
    }

    //Simple division using repeated subtractions
    //Returns Large Int array in the format:
    //0 - quotient | 1 - remainder (a % b)
    //This - dividend
    //Other - divisor
    //Executes dividend/divisor 
    public LargeInt[] division(LargeInt other) {
        //Setup
        byte[] dividend = this.getValue();
        byte[] divisor = other.getValue();
        //Make divisor same size as dividend (divisor << n bits in dividend)
        //This subtraction algorithm follows same format as long division so by normalizing the lengths, we can keep track of position the partial quotient is in
        divisor = shiftLeft(divisor, dividend.length);

        //initialize quotient to be 0
        byte[] quotient = { 0 };
        LargeInt quotientLarge = new LargeInt(quotient);
        //start with the remainder = dividend
        LargeInt remainderLarge = new LargeInt(dividend);

        //Algorithm
        //for n bits in dividend (same amount we shifted the divisor)
        for (int n = 0; n < dividend.length; n++) {
            //shift the divisor right one
            divisor = shiftRight(divisor, 1);
            LargeInt partialDivisor = new LargeInt(divisor);

            //while we can fit the current divisor into the remaining number
            while (!remainderLarge.subtract(partialDivisor).isNegative()) {
                //subtract by current amount of divisor
                remainderLarge = remainderLarge.subtract(partialDivisor);

                //calculating the correct amount of shifts for where the quotient should go
                //ex: 700/25 on step 700/250 the "2" that 250 goes into 700 should go at position 0 in the quotient array
                //next step 200/25 the "8" that 25 goes into 700 should go at position 1 in the quotient array
                //result 0-2 1-8

                //calculate the correct shifts by doing the length of dividend - n amount of shifts so far
                byte[] partialQuotient = new byte[1];
                partialQuotient[0] = 1;
                partialQuotient = shiftLeft(partialQuotient, (dividend.length - n - 1));

                //add the partial quotient to the total quotient
                quotientLarge = quotientLarge.add(new LargeInt(partialQuotient));
            }
        }
        LargeInt[] toReturn = { quotientLarge, remainderLarge };
        return toReturn;
    }

    //Helper shift methods on byte arrays
    private byte[] shiftLeft(byte[] byteArray, int shiftAmount) {
        //Left shifted array is shiftAmount longer than original
        //Ex. 25 shiftleft 2 = 2500
        byte[] shifted = new byte[byteArray.length + shiftAmount];
        //copy old values to shift array
        int i;
        for (i = 0; i < byteArray.length; i++) {
            shifted[i] = byteArray[i];
        }
        //assigning a 0 to the least significant bytes
        for (int j = 0; j < shiftAmount; j++) {
            shifted[i] = 0;
            i++;
        }
        return shifted;
    }

    private byte[] shiftRight(byte[] byteArray, int shiftAmount) {
        //Right shifted array is shiftAmount shorter than original
        byte[] shifted = new byte[byteArray.length - shiftAmount];
        //copy old values into new array
        for (int i = 0; i < shifted.length; i++) {
            shifted[i] = byteArray[i];
        }
        return shifted;
    }

    //Helper function to trim the leading unnecessary 0s added when multiplying two numbers
    //This prevents slowdowns and unnecessary growth
    //Length is the minimum length we should not trim below
    //Only trims leading 0s
    private LargeInt trimLeadingZeroes(LargeInt toTrim, int length) {
        int size = toTrim.getLength() - 1;
        //if the first byte is a 0 and we are still above the size length, trim
        while (toTrim.getValue()[0] == 0 && size > length) {
            //create new byte array with size one less
            byte[] replace = new byte[size];

            //for p-1<trim length, everything but msb
            for (int p = 1; p < toTrim.getLength(); p++) {
                replace[p - 1] = toTrim.getValue()[p];
            }
            toTrim = new LargeInt(replace);
            size--;
        }
        return toTrim;
    }

    //Extended Euclidean algorithm
    //Returns array structured as follows:
    // 0 - GCD | 1 - valid x value | 2 - valid y value
    //x and y values such that: this*x + other*y = GCD
    public LargeInt[] XGCD(LargeInt other) {
        //Determine which of the inputs is the dividend (larger) and divisor (smaller)
        //If this.subtract(other) is positive, first is larger
        if (!this.subtract(other).isNegative()) {
            return XGCDHelper(this, other);
        } else {
            return XGCDHelper(other, this);
        }
    }

    private LargeInt[] XGCDHelper(LargeInt first, LargeInt second) {
        int curr = 0;
        //matrix follows structure:
        // | a | b | a/b | a%b | GCD | s | t |
        LargeInt[][] xGCD = new LargeInt[10][7];
        //populate first line of matrix
        xGCD[curr][0] = first;
        xGCD[curr][1] = second;
        LargeInt[] division = first.division(second);
        xGCD[curr][2] = division[0];
        xGCD[curr][3] = division[1];
        LargeInt LI_ZERO = new LargeInt(ZERO);
        xGCD[curr][4] = LI_ZERO;
        xGCD[curr][5] = LI_ZERO;
        xGCD[curr][6] = LI_ZERO;
        curr++;

        //work way through matrix until GCD is calculated
        while (true) {
            //If current matrix is full, double its size
            if (curr == (xGCD.length)) {
                xGCD = resize(xGCD);
            }
            //a becomes the previous b
            xGCD[curr][0] = xGCD[curr - 1][1];
            //b becomes the previous a%b
            xGCD[curr][1] = xGCD[curr - 1][3];
            //calculate the new division
            division = xGCD[curr][0].division(xGCD[curr][1]);
            //update a/b
            xGCD[curr][2] = division[0];
            //update a%b
            xGCD[curr][3] = division[1];
            xGCD[curr][4] = LI_ZERO;
            xGCD[curr][5] = LI_ZERO;
            xGCD[curr][6] = LI_ZERO;

            //if the current a%b becomes 0, we have found the GCD and it is the current b
            if (xGCD[curr][3].isZero()) {
                xGCD[curr][4] = xGCD[curr][1];
                break;
            }
            curr++;
        }

        //at this point we know the GCD so we can do the extended euclidean algorithm to calculate s and t
        //[GCD, s, t] GCD = a * s + b * t
        //initialize the last s and t, s starts at 0, t starts at 1
        xGCD[curr][5] = LI_ZERO;
        xGCD[curr][6] = new LargeInt(ONE);
        //work backwards starting at end of matrix
        LargeInt temp;
        for (int i = curr - 1; i >= 0; i--) {
            //s becomes previous t
            xGCD[i][5] = xGCD[i + 1][6];
            //t becomes = sprevious - (a/b)*tprevious
            temp = xGCD[i + 1][5].subtract(xGCD[i][2].gradeschool(xGCD[i + 1][6]));
            //trim the unnecessary 0s from multiplication operation before putting back into matrix to prevent unnecessary growth and slowdown
            //should not trim to a smaller size than the first input
            xGCD[i][6] = trimLeadingZeroes(temp, first.getLength());
        }

        LargeInt[] toReturn = { xGCD[curr][4], xGCD[0][5], xGCD[0][6] };
        return toReturn;
    }

    //upsize LargeInt[][] by *2
    private LargeInt[][] resize(LargeInt[][] inputMatrix) {
        LargeInt[][] newMatrix = new LargeInt[inputMatrix.length * 2][7];
        for (int i = 0; i < inputMatrix.length; i++) {
            for (int j = 0; j < inputMatrix[i].length; j++) {
                newMatrix[i][j] = inputMatrix[i][j];
            }
        }
        return newMatrix;
    }

    //implementation of pseudo code presented in lecture 7
    public LargeInt modularExp(LargeInt exponent, LargeInt modulus) {
        LargeInt base = new LargeInt(this.getValue());
        LargeInt result = new LargeInt(ONE);
        BigInteger tracker = new BigInteger(exponent.getValue());

        //for k=0 to b-1 (little endian notation so actually reading the MSB first)
        for (int i = 1; i <= tracker.bitLength(); i++) {
            //if k bit == 1
            //System.out.println("CURRENT = " + (tracker.testBit(tracker.bitLength() - i)));
            if (tracker.testBit(tracker.bitLength() - i)) {
                //r = (r * base) mod n
                result = (base.gradeschool(result)).division(modulus)[1];
            }
            if (tracker.bitLength() - i > 0) {
                //r = r^2 mod n 
                result = (result.gradeschool(result)).division(modulus)[1];
            }
        }
        return result;
    }

    //Compute modular exponentiation in the form a^b % n
    //Function accepts three parameters in the following order:
    //base (this) - a | exponent - b | modulus - n
    //Based off Fast Modular Exponentiation (http://www.cs.ucf.edu/~dmarino/progcontests/modules/matexpo/RecursionFastExp.pdf)
    //& Wikipedia Modular Exponentiation
    //The idea is to split the modular exponentiation once the exponent is a power of 2 (subtract 1 if not negative)
    public LargeInt modularExpFast(LargeInt exponent, LargeInt modulus) {
        LargeInt LI_ONE = new LargeInt(ONE);
        LargeInt LI_TWO = new LargeInt(TWO);
        //Base Cases
        //Exponent == 0
        if (exponent.isZero()) {
            return LI_ONE;
        }
        //Exponent == 1
        else if ((exponent.subtract(LI_ONE)).isZero()) {
            return (new LargeInt(this.getValue()));
        }
        //Exponent % 2 == 0 (even)
        else if (exponent.division(LI_TWO)[1].isZero()) {
            //Return modExp(base*base%n, exp/2, n)
            return (this.gradeschool(this).division(modulus)[1]).modularExpFast(exponent.division(LI_TWO)[0], modulus);
        }
        //Otherwise exponent is not even
        else {
            //Return base * modExp(base, exp-1, n)%n
            return this.gradeschool(this.modularExpFast(exponent.subtract(LI_ONE), modulus)).division(modulus)[1];
        }
    }

    public LargeInt modularExpFast_BI(BigInteger exponent, BigInteger modulus) {
        BigInteger base = new BigInteger(this.getValue());

        BigInteger toReturn = modularExpFast_BI_H(base, exponent, modulus);

        return (new LargeInt(toReturn.toByteArray()));
    }

    private static BigInteger modularExpFast_BI_H(BigInteger base, BigInteger exponent, BigInteger modulus) {

        //Base Cases
        //Exponent == 0
        if (exponent.equals(BigInteger.ZERO)) {
            return BigInteger.ONE;
        }
        //Exponent == 1
        else if (exponent.equals(BigInteger.ONE)) {
            return (base);
        }
        //Exponent % 2 == 0 (even)
        else if (!exponent.testBit(0)) {
            //Return modExp(base*base%n, exp/2, n)
            return modularExpFast_BI_H((base.multiply(base).remainder(modulus)), exponent.divide(BigInteger.TWO),
                    modulus);
        }
        //Otherwise exponent is not even
        else {
            //Return base * modExp(base, exp-1, n)%n
            return base.multiply(modularExpFast_BI_H(base, exponent.subtract(BigInteger.ONE), modulus))
                    .remainder(modulus);
        }
    }

}
