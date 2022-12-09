import java.util.Random;
import java.math.BigInteger;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.Scanner;

/*
 * Program to look at the timing difference between a naive implementation of
 * modular exponentiation using grade-school multiplication vs
 * a more sophisticated method using Montgomery multiplication
 * 
 * Large numbers are represented using byte arrays generated using java's big
 * integer
 * Parts of Grade-school multiplication algorithm inherited from my
 * implementation in CS1501 project5
 * 
 * Timing difference analyzed using java time
 */

public class timingAnalysis {
    static final Random randomGen = new Random();
    static String one = "1";
    static String zero = "0";

    public static void main(String args[]) {

        //Setting up initialization
        //Command line takes 4 arguments in the following format:
        // | # of tests | length of exponent in digits | type of test (random, simple, complex) | 0 for naive 1 for fast |

        int numberOfTests = Integer.parseInt(args[0]);
        String type = args[1].toUpperCase();
        boolean fast = Boolean.parseBoolean(args[2]);
        boolean big = Boolean.parseBoolean(args[3]);

        //type if type == generate, generate new random exponent input file for testing
        if (type.equals("GENERATE")) {
            try {
                String outputFile = "input/inputRANDOM.txt";
                System.out.println("GENERATING NEW RANDOM INPUT FILE OF LENGTH " + numberOfTests);
                FileWriter fWriter = new FileWriter(outputFile);

                for (int i = 0; i < numberOfTests; i++) {
                    String curr = generateBitString(1024) + "\n";
                    fWriter.write(curr);
                }
                fWriter.close();
            } catch (IOException e) {
                System.out.println("Error creating input file");
                e.printStackTrace();
            }
            //Exit after creating input file
            System.exit(0);
        }
        try {
            //Setting up output file
            String outputFile = "output/output" + type + fast + big + ".txt";
            System.out.println("OUTPUTTING RESULTS TO FILE = " + outputFile);
            FileWriter fWriter = new FileWriter(outputFile);

            //Determining type

            //All test use fixed length base of 5 (this number is arbitrary) and fixed length modulus 150
            //RSA recommends a minimum size of 512 bits for public key which is about 15
            //Simple & Complex read from input file which contains exponents with a large portion of the bits being set to 0 or 1 respectively
            //A maximum of 20 tests can be run on simple
            //The exponent length are all 1024 bit long
            if (type.equals("RANDOM") || type.equals("SIMPLE") || type.equals("COMPLEX")) {

                System.out.println(
                        "RUNNING " + numberOfTests + " " + type + " FAST = " + fast + " (If fast == 0, using naive)");
                //reading in from input file
                String inputString = "input/input" + type + ".txt";
                try (Scanner simpleSc = new Scanner(new File(inputString))) {
                    if (big) {
                        long sum = 0;
                        int i = 0;
                        while (simpleSc.hasNextLine() && i < numberOfTests) {
                            System.out.println("TEST " + i);
                            BigInteger currLine = new BigInteger(simpleSc.nextLine(), 2);
                            long curr = testModPow_BI(currLine);
                            String currOut = "" + curr + "\n";
                            fWriter.write(currOut);
                            //currOut = "Exponent = " + (new BigInteger(exp)) + "\n";
                            fWriter.write(currOut);
                            sum += curr;
                            i++;
                        }
                        String output = "AVERAGE ON TESTS = " + (sum / (long) numberOfTests);
                        System.out.println(output);
                        simpleSc.close();
                    } else {
                        //Initializing inputs
                        long sum = 0;
                        byte[] base = (new LargeInt(generateByteArray(5))).getValue();
                        byte[] modulus = (new LargeInt(generateByteArray(150))).getValue();

                        int i = 0;
                        while (simpleSc.hasNextLine() && i < numberOfTests) {
                            System.out.println("TEST " + i);
                            //The radix 2 specifiies that this string is a binary number
                            BigInteger currLine = new BigInteger(simpleSc.nextLine(), 2);
                            byte[] exp = currLine.toByteArray();

                            long curr = testModPow(base, exp, modulus, fast);
                            String currOut = "" + curr + "\n";
                            fWriter.write(currOut);
                            currOut = "Exponent = " + (new BigInteger(exp)) + "\n";
                            fWriter.write(currOut);
                            sum += curr;
                            i++;
                        }
                        String output = "AVERAGE ON TESTS = " + (sum / (long) numberOfTests);
                        System.out.println(output);
                        fWriter.write(output);
                        simpleSc.close();
                    }

                } catch (IOException e) {
                    System.out.println("Input File Not Found");
                    e.printStackTrace();
                }

            } else {
                System.out.println("Please specify the type as: random, simple, or complex");
            }
            fWriter.close();

        } catch (

        IOException e) {
            System.out.println("Error creating output file");

        }
    }

    // Method to generate bit strings
    // Parameter length is how many bits long
    static String generateBitString(int length) {
        String toReturn = "";
        for (int i = 0; i < length; i++) {
            //assuring the most significant bit is a 1 and therefore the resulting string will have length 1024 bit
            if (i == 0) {
                toReturn += one;
            } else if (randomGen.nextBoolean()) {
                toReturn += zero;
            } else {
                toReturn += one;
            }
        }
        return toReturn;
    }

    // Method to generate large amounts of large Big Ints
    static void generateLargeInt(int amount, int length) {
        for (int i = 0; i < amount; i++) {
            LargeInt bigboy = new LargeInt(generateByteArray(length));
            System.out.println("Test generation # " + i + " = " + (new BigInteger(bigboy.getValue())));
        }
    }

    // Using java big integer to generate random byte arrays of input length
    static byte[] generateByteArray(int length) {
        int min = 0;
        int max = 9;
        int range = max - min + 1;
        String newBigInt = "";
        for (int i = 0; i < length; i++) {
            int random = (int) (Math.random() * range) + min;
            newBigInt += String.valueOf(random);
        }
        return (new BigInteger(newBigInt)).toByteArray();
    }

    static long testModPow_BI(BigInteger exp) {

        LargeInt LI_base = new LargeInt(generateByteArray(5));
        LargeInt LI_exponent = new LargeInt(generateByteArray(300));
        LargeInt LI_modulus = new LargeInt(generateByteArray(150));
        BigInteger BI_base = new BigInteger(LI_base.getValue());
        BigInteger BI_exponent = exp;
        BigInteger BI_modulus = new BigInteger(LI_modulus.getValue());
        BigInteger modpow = BI_base.modPow(BI_exponent, BI_modulus);

        long t1;
        long t2;
        LargeInt LI_result;
        //if fast == 0, use naive implementation
        System.out.println("BIG BOY TEST");
        t1 = System.nanoTime();
        LI_result = LI_base.modularExpFast_BI(BI_exponent, BI_modulus);
        t2 = System.nanoTime();

        //if my calculated modpow value is not the same as java big int
        //terminate program
        if (modpow.compareTo(new BigInteger(LI_result.getValue())) != 0) {
            System.out.println("Base = " + BI_base);
            System.out.println("Exponent = " + BI_exponent);
            System.out.println("Modulus = " + BI_modulus);
            System.out.println("Result          = " + (new BigInteger(LI_result.getValue())));
            System.out.println("Expected result = " + modpow);
            System.out.println("TEST FAILED!");
            System.exit(0);
        }
        return t2 - t1;

    }

    static long testModPow(byte[] base, byte[] exp, byte[] modulus, boolean fast) {

        LargeInt LI_base = new LargeInt(base);
        LargeInt LI_exponent = new LargeInt(exp);
        LargeInt LI_modulus = new LargeInt(modulus);
        BigInteger BI_base = new BigInteger(LI_base.getValue());
        BigInteger BI_exponent = new BigInteger(LI_exponent.getValue());
        BigInteger BI_modulus = new BigInteger(LI_modulus.getValue());
        BigInteger modpow = BI_base.modPow(BI_exponent, BI_modulus);

        long t1;
        long t2;
        LargeInt LI_result;
        //if fast == 0, use naive implementation
        if (!fast) {
            t1 = System.nanoTime();
            LI_result = LI_base.modularExp(LI_exponent, LI_modulus);
            t2 = System.nanoTime();
        } else {
            System.out.println("FAST");
            t1 = System.nanoTime();
            LI_result = LI_base.modularExpFast(LI_exponent, LI_modulus);
            t2 = System.nanoTime();
        }

        //if my calculated modpow value is not the same as java big int
        //terminate program
        if (modpow.compareTo(new BigInteger(LI_result.getValue())) != 0) {
            System.out.println("Base = " + BI_base);
            System.out.println("Exponent = " + BI_exponent);
            System.out.println("Modulus = " + BI_modulus);
            System.out.println("Result          = " + (new BigInteger(LI_result.getValue())));
            System.out.println("Expected result = " + modpow);
            System.out.println("TEST FAILED!");
            System.exit(0);
        }
        return t2 - t1;

    }

    // static long testModPow(byte[] base, byte[] exp, byte[] modulus, int fast) {

    //     LargeInt LI_base = new LargeInt(base);
    //     LargeInt LI_exponent = new LargeInt(exp);
    //     LargeInt LI_modulus = new LargeInt(modulus);
    //     BigInteger BI_base = new BigInteger(LI_base.getValue());
    //     BigInteger BI_exponent = new BigInteger(LI_exponent.getValue());
    //     BigInteger BI_modulus = new BigInteger(LI_modulus.getValue());

    //     BigInteger modpow = BI_base.modPow(BI_exponent, BI_modulus);

    //     //Testing Large Integer Method
    //     // System.out.println("Test Simple Mod Pow: " + BI_base + "^" + BI_exponent + " % " + BI_modulus);
    //     // System.out.println("Base = " + BI_base);
    //     // System.out.println("Exponent = " + BI_exponent);
    //     // System.out.println("Modulus = " + BI_modulus);
    //     long t1 = System.nanoTime();
    //     LargeInt LI_result = LI_base.modularExp(LI_exponent, LI_modulus);
    //     long t2 = System.nanoTime();
    //     // System.out.println("Result = " + (new BigInteger(LI_result.getValue())));
    //     // System.out.println("Expected result = " + modpow);

    //     // if (modpow.compareTo(new BigInteger((LI_result).getValue())) == 0) {
    //     //     System.out.println("Test SUCCESS WOOOOOOOOOOOOOOOOOOOOOOOOOO");
    //     // } else {
    //     //     System.out.println("Test FAIL");
    //     // }
    //     return t2 - t1;

    // }
    //Debugging code for testing
    static boolean testGCD() {
        LargeInt LI_x = new LargeInt(generateByteArray(2048));
        LargeInt LI_y = new LargeInt(generateByteArray(2));
        BigInteger BI_x = new BigInteger(LI_x.getValue());
        BigInteger BI_y = new BigInteger(LI_y.getValue());

        BigInteger gcd = BI_x.gcd(BI_y);

        //Testing Large Integer Method
        System.out.println("Test Simple GCD:");
        System.out.println("X = " + BI_x);
        System.out.println("Y = " + BI_y);
        LargeInt[] LI_result = LI_x.XGCD(LI_y);
        System.out.println("GCD = " + (new BigInteger((LI_result[0]).getValue())));
        System.out.println("S = " + (new BigInteger((LI_result[1]).getValue())));
        System.out.println("T = " + (new BigInteger((LI_result[2]).getValue())));
        System.out.println("Expected GCD = " + gcd);

        if (gcd.compareTo(new BigInteger((LI_result[0]).getValue())) == 0) {
            System.out.println("Test success");
            return true;
        }
        System.out.println("Test failed");
        return false;
    }

    //Testing Methods for Implementations of Basic Arithmetic
    //Testing Implementation of Addition
    static void testAdd() {
        LargeInt a = new LargeInt(generateByteArray(1024));
        LargeInt b = new LargeInt(generateByteArray(1024));
        System.out.println("Test Addition:");
        System.out.println("a = " + a);
        System.out.println("b = " + b);
        System.out.println("Result = " + (new BigInteger((a.add(b)).getValue())));
        BigInteger aB = new BigInteger(a.getValue());
        BigInteger bB = new BigInteger(b.getValue());
        BigInteger BB = aB.add(bB);
        if (BB.compareTo(new BigInteger((a.add(b)).getValue())) == 0) {
            System.out.println("Test success");
        } else {
            System.out.println("Test failed");
        }

    }

    //Testing Implementation of Subtraction
    static void testSubtract() {
        LargeInt a = new LargeInt(generateByteArray(2048));
        LargeInt b = new LargeInt(generateByteArray(1024));
        System.out.println("Test Subtract:");
        System.out.println("a = " + a);
        System.out.println("b = " + b);
        System.out.println("Result = " + (new BigInteger((a.subtract(b)).getValue())));
        BigInteger aB = new BigInteger(a.getValue());
        BigInteger bB = new BigInteger(b.getValue());
        BigInteger BB = aB.subtract(bB);
        if (BB.compareTo(new BigInteger((a.subtract(b)).getValue())) == 0) {
            System.out.println("Test success");
        } else {
            System.out.println("Test failed");
        }
    }

    //Testing Implementation of Gradeschool Multiplication
    static boolean testGradeschool() {
        LargeInt a = new LargeInt(generateByteArray(2048));
        LargeInt b = new LargeInt(generateByteArray(1024));
        //adding a chance that one of the numbers can be negative
        if ((int) Math.random() < .2) {
            if (((int) Math.random() < .1)) {
                a = a.negate();
            } else {
                b = b.negate();
            }
        }
        BigInteger aB = new BigInteger(a.getValue());
        BigInteger bB = new BigInteger(b.getValue());
        BigInteger BB = aB.multiply(bB);
        System.out.println("Test Gradeschool:");
        System.out.println("a = " + aB);
        System.out.println("b = " + bB);
        System.out.println("Result = " + (new BigInteger((a.gradeschool(b)).getValue())));
        System.out.println("Expected = " + BB);
        if (BB.compareTo(new BigInteger((a.gradeschool(b)).getValue())) == 0) {
            System.out.println("Test success");
            return true;
        } else {
            System.out.println("Test failed");
            return false;
        }
    }

    static boolean testDivision() {
        LargeInt a = new LargeInt(generateByteArray(2048));
        LargeInt b = new LargeInt(generateByteArray(256));
        BigInteger dividend = new BigInteger(a.getValue());
        BigInteger divisor = new BigInteger(b.getValue());
        BigInteger quotient = dividend.divide(divisor);
        BigInteger remainder = dividend.mod(divisor);
        System.out.println("Test Simple Division:");
        System.out.println("Dividend = " + dividend);
        System.out.println("Divisor = " + divisor);
        System.out.println("Quotient = " + (new BigInteger((a.division(b)[0]).getValue())));
        System.out.println("Remainder = " + (new BigInteger((a.division(b)[1]).getValue())));
        System.out.println("Expected quotient = " + quotient);
        System.out.println("Expected remainder = " + remainder);
        if (quotient.compareTo(new BigInteger((a.division(b)[0]).getValue())) == 0) {
            if (remainder.compareTo(new BigInteger((a.division(b)[1]).getValue())) == 0) {
                System.out.println("Test success");
                return true;
            }
        }
        System.out.println("Test failed");
        return false;
    }
}
