/*
    Access control policy simulated around Campus Security
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

//Stores Permission,Location pairs
//Groups are then assigned lists of permissions
class Permission {
    private String permission;
    private String location;

    public Permission(String permission, String location) {
        this.permission = permission;
        this.location = location;
    }

    public String getPermission() {
        return this.permission;
    }

    public String getLocation() {
        return this.location;
    }

    @Override
    public String toString() {
        return "[permission=" + permission + " | location=" + location + "]";
    }

    // override equals to allow comparison in remove method
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Permission)) {
            return false;
        }
        Permission p = (Permission) o;

        if (this.getPermission().equals(p.getPermission())) {
            if (this.getLocation().equals(p.getLocation())) {
                return true;
            }
        }
        return false;

    }
}

// User object to store which group the user belongs to and permissions specific to user
class User {
    private String group;
    private ArrayList<Permission> permissions;

    public User(String group) {
        this.group = group;
        this.permissions = new ArrayList<Permission>();
    }

    public String getGroup() {
        return this.group;
    }

    public ArrayList<Permission> getPermissions() {
        return this.permissions;
    }

    public void addPermission(Permission p) {
        this.permissions.add(p);
    }
}

// Group object to store the group's permissions and a list of other groups it inherits from
class Group {
    private ArrayList<Permission> groupPermissions;
    private ArrayList<String> inherited;
    private ArrayList<String> admin;

    public Group() {
        this.groupPermissions = new ArrayList<Permission>();
        this.inherited = new ArrayList<String>();
        this.admin = new ArrayList<String>();
    }

    public ArrayList<Permission> getPermissions() {
        return this.groupPermissions;
    }

    public void removePermission(Permission p) {
        this.groupPermissions.remove(p);
    }

    public void addPermission(Permission p) {
        this.groupPermissions.add(p);
    }

    public ArrayList<String> getInherited() {
        return this.inherited;
    }

    public void addInherited(String i) {
        this.inherited.add(i);
    }

    public ArrayList<String> getAdmin() {
        return this.admin;
    }

    public void addAdmin(String a) {
        this.admin.add(a);
    }
}

public class accessControl {

    private HashMap<String, Group> groups;
    private HashMap<String, User> users;
    private boolean access;
    private final char comment = '#';
    private final String delimiter = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

    public accessControl() {
        groups = new HashMap<String, Group>();
        users = new HashMap<String, User>();
        access = false;
    }

    public void inputFile(String type, String filePath) {
        try (BufferedReader inputFile = new BufferedReader(new FileReader(filePath))) {
            String line = inputFile.readLine();
            // groups file
            if (type.equals("1")) {
                while (line != null) {
                    //skip comment lines
                    if (!(line.charAt(0) == comment)) {
                        String[] curr = line.split(delimiter, -1);
                        // reading in group specific permission
                        if (curr.length == 3) {
                            // group does not exist yet
                            if (!groups.containsKey(curr[0])) {
                                // create new group
                                Group newGroup = new Group();
                                groups.put(curr[0], newGroup);
                            }
                            // create new permission
                            Permission newPerm = new Permission(curr[1], curr[2]);
                            // retrieve group
                            Group group = groups.get(curr[0]);
                            // add permission to group
                            group.addPermission(newPerm);
                            // put back into hashmap
                            groups.put(curr[0], group);
                        }
                        // reading in inherited groups
                        else if (curr.length == 2) {
                            // group does not exist yet
                            if (!groups.containsKey(curr[0])) {
                                // create new group
                                Group newGroup = new Group();
                                groups.put(curr[0], newGroup);
                            }
                            // retrieve group
                            Group group = groups.get(curr[0]);
                            // add inherited group
                            group.addInherited(curr[1]);
                            // put back into hashmap
                            groups.put(curr[0], group);

                        } else {
                            System.out.println("Incorrect groups.csv format");
                        }

                    }
                    line = inputFile.readLine();
                }
                System.out.println("Successfully read in groups file");
            }
            // users file
            else if (type.equals("2")) {
                while (line != null) {
                    //skip comment lines
                    if (!(line.charAt(0) == comment)) {
                        String[] curr = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                        // user belonging to group
                        if (curr.length == 2) {
                            User newUser = new User(curr[1]);
                            users.put(curr[0], newUser);
                        }
                        // user specific permission
                        else if (curr.length == 3) {
                            // if user does not exist yet
                            if (!users.containsKey(curr[0])) {
                                //create new user
                                User newUser = new User("");
                                users.put(curr[0], newUser);
                            }
                            // add permissions for user
                            Permission newPerm = new Permission(curr[1], curr[2]);
                            User user;
                            // retrieve user from hashmap
                            user = users.get(curr[0]);
                            // add permission
                            user.addPermission(newPerm);
                            // put back into hashmap
                            users.put(curr[0], user);
                        } else {
                            System.out.println("Incorrect users.csv format");
                        }

                    }
                    line = inputFile.readLine();
                }
                System.out.println("Successfully read in users file");
            }
            // administrative delegation file
            else if (type.equals("3")) {
                while (line != null) {
                    //skip comment lines
                    if (!(line.charAt(0) == comment)) {
                        String[] curr = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                        if (curr.length == 2) {
                            // group does not exist yet
                            if (!groups.containsKey(curr[0])) {
                                // create new group
                                Group newGroup = new Group();
                                groups.put(curr[0], newGroup);
                            }
                            // retrive group
                            Group group = groups.get(curr[0]);
                            // add admin to group
                            group.addAdmin(curr[1]);
                            // put back into hashmap
                            groups.put(curr[0], group);
                        } else {
                            System.out.println("Incorrect delegation.csv format");
                        }

                    }
                    line = inputFile.readLine();
                }
                System.out.println("Successfully read in delegation file");
            } else {
                System.out.println("Incorrect format");
            }
            inputFile.close();
        } catch (IOException e) {
            System.out.println("Error reading file");
            e.printStackTrace();
        }
    }

    // search the current hashmaps
    public void query(String type, String first, String second) {
        System.out.println("-------------------------------------");
        // what users are part of group x
        if (type.equals("1")) {
            String group = first;
            // see if group is part of system
            if (groups.containsKey(group)) {
                System.out.println("Listing users part of " + group);
                for (Map.Entry<String, User> entry : users.entrySet()) {
                    //if user part of group
                    if (entry.getValue().getGroup().equals(group)) {
                        System.out.println(entry.getKey());
                    }
                }
            } else {
                System.out.println("Group not found");
            }

        }
        // what permissions does user x have?
        else if (type.equals("2")) {
            String user = first;
            // see if user is part of system
            if (users.containsKey(user)) {

                // find which group user belongs to
                // get group permissions
                String group = users.get(user).getGroup();
                if (group.isEmpty()) {
                    System.out.println("User not part of a group");
                } else {
                    System.out.println("Permissions From Group: " + group);
                    printGroup(groups.get(group));
                }

                // user permissions
                System.out.println(user + " has permissions:");
                ArrayList<Permission> curr = users.get(user).getPermissions();
                for (Permission p : curr) {
                    System.out.println(p);
                }
            } else {
                System.out.println("User not found");
            }
        }
        // what permissions does group x have?
        else if (type.equals("3")) {
            String group = first;
            // see if group is part of system
            if (groups.containsKey(group)) {
                System.out.println("Permissions From Group: " + group);
                printGroup(groups.get(group));
            } else {
                System.out.println("Group not found");
            }

        }
        // can user ___ access location ___? (and with what permission?)
        else if (type.equals("4")) {
            access = false;
            String user = first;
            String location = second;
            // see if user exists
            if (users.containsKey(user)) {
                // check user's group permissions
                // must also check inherited permissions
                // if user part of group
                if (!users.get(user).getGroup().isEmpty()) {
                    String group = users.get(user).getGroup();
                    findPermission(user, group, location, groups.get(group), false);
                }
                // check user's permissions
                ArrayList<Permission> userPerm = users.get(user).getPermissions();
                for (Permission p : userPerm) {
                    if (p.getLocation().equals(location)) {
                        access = true;
                        System.out.println(user + " has permission:");
                        System.out.println(p);
                    }
                }
                if (!access)
                    System.out.println("No, they do not have permission for " + location);
            } else {
                System.out.println("User not found");
            }
        }
        // who has permission ____ to location ___? (list just users)
        else if (type.equals("5")) {
            String permType = first;
            String location = second;

            // find groups that have access with correct permission
            ArrayList<String> groupAccess = new ArrayList<String>();
            for (Map.Entry<String, Group> entry : groups.entrySet()) {
                // check group's permission
                ArrayList<Permission> curr = entry.getValue().getPermissions();
                for (Permission p : curr) {
                    // if permission is correct and contains correct location
                    if (p.getPermission().equals(permType) && p.getLocation().equals(location)) {
                        //groupAccess.put(entry.getKey(), p);
                        groupAccess.add(entry.getKey());
                    }
                }
            }
            // search users
            ArrayList<String> usersAccess = new ArrayList<String>();
            for (Map.Entry<String, User> entry : users.entrySet()) {
                // if user belongs to group that has permission
                if (groupAccess.contains(entry.getValue().getGroup())) {
                    System.out.println(entry.getKey() + " is part of group " + entry.getValue().getGroup()
                            + " which has permission");
                    // if (!usersAccess.contains(entry.getKey())) {
                    //     usersAccess.add(entry.getKey());
                    // }
                }
                // // if user inherits group that has permission
                else {
                    String user = entry.getKey();
                    String group = entry.getValue().getGroup();
                    Group g = groups.get(group);
                    if (groups.containsKey(group)) {
                        findPermission(user, group, location, g, false, permType);
                    }

                }
                // if user has individual permission
                // check if user has permission
                ArrayList<Permission> curr = entry.getValue().getPermissions();
                for (Permission p : curr) {
                    if (p.getPermission().equals(permType) && p.getLocation().equals(location)) {
                        // if (!usersAccess.contains(entry.getKey())) {
                        //     usersAccess.add(entry.getKey());
                        // }
                        System.out.println(entry.getKey() + " has individual permission:");
                        System.out.println(p);
                    }
                }
            }
            // print users found
            // for (String u : usersAccess) {
            //     System.out.println(u);
            // }
        } else {
            System.out.println("Incorrect format");
        }
        System.out.println("-------------------------------------");
    }

    // recursive method to help print groups and inherited groups
    private void printGroup(Group g) {
        ArrayList<Permission> groupPerms = g.getPermissions();
        if (groupPerms.isEmpty()) {
            System.out.println("No Group Permissions");
        } else {
            for (Permission p : groupPerms) {
                System.out.println(p);
            }
        }
        System.out.println();
        ArrayList<String> inherited = g.getInherited();
        if (!inherited.isEmpty()) {
            for (String s : inherited) {
                System.out.println("Inherited From Group: " + s);
                printGroup(groups.get(s));
            }
        }
        ArrayList<String> admin = g.getAdmin();
        if (!admin.isEmpty()) {
            for (String a : admin) {
                System.out.println("Administrator Of Group: " + a);
            }
        }
    }

    // debug print statements to visualize what is loaded
    public void debug(String type) {
        boolean override = false;
        System.out.println("-----------------------------");
        if (type.equals("all")) {
            override = true;
        }
        if (type.equals("group") || override) {
            System.out.println("Group Policy");
            System.out.println("Current group size = " + groups.size());
            for (Map.Entry<String, Group> entry : groups.entrySet()) {
                System.out.println("***************");
                System.out.println("Group: " + entry.getKey());
                // printing inherited permissions
                // recursion is used since groups inherited can also inherit permissions
                // from other groups, chain of command Grad Student <- Student <- Guest <- Public for example
                printGroup(entry.getValue());
            }
        }
        if (type.equals("user") || override) {
            System.out.println("Current user size = " + users.size());
            for (Map.Entry<String, User> entry : users.entrySet()) {
                System.out.println(entry.getKey() + " | " + "Group=" + entry.getValue().getGroup());
                System.out.println("User specific permissions:");
                ArrayList<Permission> curr = entry.getValue().getPermissions();
                for (Permission p : curr) {
                    System.out.println(p);
                }
            }
        }
        System.out.println("-----------------------------");
    }

    public void reset() {
        groups = new HashMap<String, Group>();
        users = new HashMap<String, User>();
        System.out.println("Reset.....");
    }

    // helper method to find and print users/group/inherited where a permission matches a desired location
    private void findPermission(String user, String group, String location, Group g, boolean inherit, String permType) {
        ArrayList<Permission> groupPerms = g.getPermissions();
        // check current group permissions
        if (!groupPerms.isEmpty()) {
            for (Permission p : groupPerms) {
                if (p.getLocation().equals(location) && p.getPermission().equals(permType)) {
                    // if inherited
                    if (inherit) {
                        System.out.println(user + " inherits group " + group + " which has permission:");
                        System.out.println(p);
                        access = true;
                    } else {
                        System.out.println(user + " is part of group " + group +
                                " which has permission:");
                        System.out.println(p);
                        access = true;
                    }

                }
            }
        }
        // find groups inherited and check those
        ArrayList<String> inherited = g.getInherited();
        if (inherited.isEmpty()) {
            return;
        } else {
            for (String s : inherited) {
                if (!groups.get(s).equals(group)) {
                    findPermission(user, s, location, groups.get(s), true, permType);
                }

            }
        }
    }

    // helper method to find and print users/group/inherited where a permission matches a desired location
    private void findPermission(String user, String group, String location, Group g, boolean inherit) {
        ArrayList<Permission> groupPerms = g.getPermissions();
        // check current group permissions
        if (!groupPerms.isEmpty()) {
            for (Permission p : groupPerms) {
                if (p.getLocation().equals(location)) {
                    // if inherited
                    if (inherit) {
                        System.out.println(user + " inherits group " + group + " which has permission:");
                        System.out.println(p);
                        access = true;
                    } else {
                        System.out.println(user + " is part of group " + group +
                                " which has permission:");
                        System.out.println(p);
                        access = true;
                    }

                }
            }
        }
        // find groups inherited and check those
        ArrayList<String> inherited = g.getInherited();
        if (inherited.isEmpty()) {
            return;
        } else {
            for (String s : inherited) {
                if (!groups.get(s).equals(group)) {
                    findPermission(user, s, location, groups.get(s), true);
                }

            }
        }
    }

    // Administrative methods
    // what does groups does input group administrate? (returning inherited arraylist)
    public ArrayList<String> adminWho(String group) {
        Group g = groups.get(group);
        return g.getAdmin();
    }

    // check if input group exists
    public boolean groupExist(String group) {
        if (groups.containsKey(group)) {
            return true;
        } else
            return false;
    }

    // add perm 
    public void addPerm(String group, String permission, String location) {
        // create new permission
        Permission newPerm = new Permission(permission, location);
        // retrieve group
        Group g = groups.get(group);
        // add permission to group
        g.addPermission(newPerm);
        // put back into hashmap
        groups.put(group, g);
    }

    // remove perm
    public void removePerm(String group, String permission, String location) {
        // create permission
        Permission newPerm = new Permission(permission, location);
        // retrieve group
        Group g = groups.get(group);
        // remove permission from group
        g.removePermission(newPerm);
        // put back into hashmap
        groups.put(group, g);
    }
}