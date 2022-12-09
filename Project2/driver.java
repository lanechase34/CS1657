
/*
    Driver code for accessControl
    Chase Lane cdl52
*/
import java.util.HashMap;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.Iterator;
import java.util.Map;
import java.io.FileWriter;

public class driver {
    static ArrayList<String> genLocations;
    static ArrayList<String> genGroups;
    static ArrayList<String> genPerms;
    static HashMap<String, ArrayList<String>> inherit;
    static boolean global;

    public static void main(String args[]) {
        Scanner input = new Scanner(System.in);
        accessControl AC = new accessControl();
        inherit = new HashMap<String, ArrayList<String>>();
        global = true;
        // set flags at runtime
        if (args.length >= 1) {
            // command to read in all policy files so a script can be ran
            // format followS: java accessControl -1 groups.csv users.csv delegation.csv
            if (args[0].equals("-1") && args.length == 4) {
                AC.inputFile("1", args[1]);
                AC.inputFile("2", args[2]);
                AC.inputFile("3", args[3]);
            }

            // execute generated test policies and record runtime
            if (args[0].equals("-2")) {
                System.out.println("RUNNING TESTS");
                String start = "./policies/g";
                String ge = "/groups.csv";
                String ue = "/users.csv";
                long s;
                long e;
                long result;
                String g;
                String u;
                for (int i = 1; i <= 6; i++) {
                    g = "";
                    u = "";
                    g = start + i + ge;
                    u = start + i + ue;
                    s = System.nanoTime();
                    AC.inputFile("1", g);
                    AC.inputFile("2", u);
                    e = System.nanoTime();

                    AC.reset();
                    result = e - s;
                    System.out.println("Runtime of g" + i + " = " + result);

                }
            }
        }

        // normal interface
        System.out.println("Please type a command. To view list of available commands, type 'l'");
        while (true) {
            String command = input.nextLine();
            if (command.equals("l")) {
                System.out.println("c - (change) change role into an administrative entity");
                System.out.println("d - (debug) list current sizes of users/groups/locations policies");
                System.out.println("g - (generate) generate random policies of varying size for testing");
                System.out.println("i - (input) read in policy from input file");
                System.out.println("l - (list) view available commands");
                System.out.println("q - (query) search regarding the policy");
                System.out.println("r - (reset) clear current policies");
                System.out.println("s - (shutdown) exit program");
            }
            // read in from policy files
            else if (command.equals("i")) {
                System.out.println("1 - groups, 2 - users, 3 - delegation");
                System.out.println("'#' 'path to csv'");
                command = input.nextLine();
                String[] commandArr = command.split(" ");
                // validating input
                if (commandArr.length == 2 && commandArr[1] != null) {
                    AC.inputFile(commandArr[0], commandArr[1]);
                } else {
                    System.out.println("Incorrect input format");
                }
            }
            // query the current policy
            else if (command.equals("q")) {
                System.out.println("List Of Available Queries:");
                System.out.println("*Surround each search term in double quotation marks*");
                System.out.println("1 - What users are part of group ___?");
                System.out.println("2 - What permissions does user ___ have?");
                System.out.println("3 - What permissions does group ___ have?");
                System.out.println("4 - Can user ___ access ___?");
                System.out.println(
                        "5 - Who has ___ permission for location ___? (List users with specific permission (restricted,partial,total) for location)");
                //System.out.println("6 - Who has permission for location ___? (List users and their permission)");
                System.out.println("# fillIn fillIn(4&5 only)");

                command = input.nextLine();
                // split input line on space but avoid spaces in " "
                String[] search = command.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (search.length > 1) {
                    // cleaning up input parameters before calling query
                    String firstIn = search[1].replaceAll("^\"|\"$", "");
                    if (search[0].equals("4") || search[0].equals("5")) {
                        String secondIn = search[2].replaceAll("^\"|\"$", "");
                        AC.query(search[0], firstIn, secondIn);
                    } else {
                        AC.query(search[0], firstIn, "");
                    }
                } else {
                    System.out.println("Incorrect format");
                }
            }
            // prints total size of each hashmap used to store policy and what is conatined
            else if (command.equals("d")) {
                System.out.println("Choose An Option");
                System.out.println("all - print all debug");
                System.out.println("group - debug group");
                System.out.println("user - debug user");
                command = input.nextLine();
                AC.debug(command);
            }
            // reset the current loaded policy
            else if (command.equals("r")) {
                AC.reset();
            }
            // change role for administrative entity
            else if (command.equals("c")) {
                System.out.println("Choose a Group (surround in double quotation marks)");
                command = input.nextLine();
                String[] commandArr = command.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                String role = commandArr[0].replaceAll("^\"|\"$", "");

                if (AC.groupExist(role)) {
                    ArrayList<String> admin = AC.adminWho(role);
                    System.out.println(role + " is admin of groups:");
                    for (String a : admin) {
                        System.out.println(a);
                    }
                    System.out.println("Choose An Option");
                    System.out.println("a - Add a permission to group");
                    System.out.println("r - Remove a permission from a group");
                    System.out.println("q - Quit");

                    System.out.println("Format:");
                    System.out.println("Code Group Permission Location");
                    System.out.println("Surround in double quotes");

                    while (true) {
                        command = input.nextLine();
                        if (command.equals("q")) {
                            System.out.println("Exiting administrator...");
                            break;
                        }
                        commandArr = command.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                        if (commandArr.length == 4) {
                            String group = commandArr[1].replaceAll("^\"|\"$", "");
                            // check if current user is admin of group
                            if (admin.contains(group)) {
                                String permission = commandArr[2].replaceAll("^\"|\"$", "");
                                String location = commandArr[3].replaceAll("^\"|\"$", "");
                                // adding permission to group
                                if (commandArr[0].equals("a")) {
                                    AC.addPerm(group, permission, location);
                                    System.out.println("Successfully added permission");
                                    System.out.println();
                                }
                                // removing permission to group
                                else if (commandArr[0].equals("r")) {
                                    AC.removePerm(group, permission, location);
                                    System.out.println("Successfully removed permission");
                                    System.out.println();
                                } else {
                                    System.out.println("Incorrect format");
                                }
                            } else {
                                System.out.println("User not administrator of group " + group);
                                System.out.println();
                            }
                        } else {
                            System.out.println("Incorrect format (needs to be 4 parameters long)");
                        }
                    }
                } else {
                    System.out.println("Group not found");
                }
            }
            // generate random policies
            else if (command.equals("g")) {
                System.out.println("Generate random policies:");
                System.out.println(
                        "Complexity refers to how complex the role inheritance and individual permissions is (100 - high, 0 - low)");

                System.out.println("#users #groups #locations #complexity(0-100)");

                command = input.nextLine();
                String[] commandArr = command.split(" ");
                if (commandArr.length == 4) {

                    // Initialize permissions (3 different ones)
                    genPerms = new ArrayList<String>();
                    genPerms.add("x");
                    genPerms.add("y");
                    genPerms.add("z");
                    genLocations = new ArrayList<String>();
                    genGroups = new ArrayList<String>();

                    // Generate locations
                    generateLocations(Integer.parseInt(commandArr[2]));

                    String groupsOutput = "policies/output/groups.csv";
                    String usersOutput = "policies/output/users.csv";

                    // generating groups file
                    generateGroup(groupsOutput, Integer.parseInt(commandArr[1]), Integer.parseInt(commandArr[3]));
                    // generating users file
                    generateUsers(usersOutput, Integer.parseInt(commandArr[0]), Integer.parseInt(commandArr[3]));
                }

            }
            // exit
            else if (command.equals("s")) {
                break;

            } else {
                System.out.println("Please try another command (l to view commands)");
            }
        }
        System.out.println("Shutting down......");
        input.close();
    }

    static void generateGroup(String file, int num, int complex) {
        try {
            FileWriter fWriter = new FileWriter(file);
            String grp = "grp";
            int min;
            int max;
            int r;
            String output;
            String curr;
            String rGroup1;
            String rGroup2;
            ArrayList<String> a;
            for (int i = 1; i <= num; i++) {
                genGroups.add(grp + i);
                output = "";
                curr = grp + i;

                // add group to hashmap
                ArrayList<String> currList = new ArrayList<String>();
                inherit.put(curr, currList);

                output = output + curr + ",";
                // get random permission
                min = 0;
                max = 2;
                r = (int) Math.floor(Math.random() * (max - min + 1) + min);
                output = output + genPerms.get(r) + ",";
                // get random location
                min = 0;
                max = genLocations.size() - 1;
                r = (int) Math.floor(Math.random() * (max - min + 1) + min);
                output = output + genLocations.get(r) + "\n";
                //System.out.println(output);
                fWriter.write(output);
                // generate inheritance
                min = 0;
                max = 100;
                r = (int) Math.floor(Math.random() * (max - min + 1) + min);
                // groups can randomly inherit another group
                if (r <= complex) {
                    output = "";
                    output = output + curr + ",";
                    // choose a random group
                    min = 1;
                    max = num;
                    r = (int) Math.floor(Math.random() * (max - min + 1) + min);
                    rGroup1 = grp + r;

                    global = true;
                    if (!rGroup1.equals(curr)) {
                        if (inherit.containsKey(rGroup1)) {
                            for (String s : inherit.get(rGroup1)) {
                                checkInherit(s, curr);
                            }
                        }
                        if (global) {
                            a = inherit.get(curr);
                            a.add(rGroup1);
                            inherit.put(curr, a);
                            output = output + rGroup1 + "\n";
                            fWriter.write(output);
                        }

                    }

                    // // 5 % chance to inherit another group
                    // if (r <= 5) {
                    //     output = "";
                    //     output = output + curr + ",";
                    //     // choose a random group
                    //     min = 1;
                    //     max = num;
                    //     r = (int) Math.floor(Math.random() * (max - min + 1) + min);
                    //     rGroup2 = grp + r;

                    //     // check again

                    //         if (!rGroup2.equals(rGroup1) && !rGroup2.equals(curr)) {

                    //             output = output + rGroup2 + "\n";
                    //             fWriter.write(output);
                    //         }
                    //     }

                    // }

                }
            }
            fWriter.close();
            System.out.println("Inherit size " + inherit.size());
        } catch (

        IOException e) {
            System.out.println("Error creating groups file");
            e.printStackTrace();
        }
        System.out.println("Successfully generated");
    }

    static void generateUsers(String file, int num, int complex) {
        try {
            FileWriter fWriter = new FileWriter(file);
            String usr = "usr";
            int min;
            int max;
            int r;
            String output;
            String curr;
            for (int i = 1; i <= num; i++) {
                output = "";
                curr = usr + i;
                output = output + curr + ",";

                // get random group
                min = 0;
                max = genGroups.size() - 1;
                r = (int) Math.floor(Math.random() * (max - min + 1) + min);
                output = output + genGroups.get(r) + "\n";
                //System.out.println(output);
                fWriter.write(output);

                // generate individual perm
                min = 0;
                max = 100;
                r = (int) Math.floor(Math.random() * (max - min + 1) + min);
                if (r <= complex) {
                    output = "";
                    output = output + curr + ",";
                    // get random permission
                    min = 0;
                    max = 2;
                    r = (int) Math.floor(Math.random() * (max - min + 1) + min);
                    output = output + genPerms.get(r) + ",";
                    // get random location
                    min = 0;
                    max = genLocations.size() - 1;
                    r = (int) Math.floor(Math.random() * (max - min + 1) + min);
                    output = output + genLocations.get(r) + "\n";
                    fWriter.write(output);
                }
            }
            fWriter.close();
        } catch (IOException e) {
            System.out.println("Error creating users file");
            e.printStackTrace();
        }
        System.out.println("Successfully generated");
    }

    static void generateLocations(int num) {
        String loc = "loc";
        for (int i = 1; i <= num; i++) {
            genLocations.add(loc + i);
        }
    }

    static void checkInherit(String group, String target) {
        // check current group inherited
        if (inherit.containsKey(group)) {
            if (inherit.get(group).contains(target)) {
                global = false;
            }
            for (String s : inherit.get(group)) {
                checkInherit(s, target);
            }
        }
    }

}
