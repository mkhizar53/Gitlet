package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Class that essentially controls all the commands that are given
 * into the Gitlet.
 * @author Mohammad Khizar
 */

public class Command {
    /**
     * A Hashmap contaning all the commands.
     */
    private HashMap<String, Consumer<String[]>> commands = new HashMap<>();

    /**
     * Constructor for the controller class.
     */

    public Command() {
        commands.put("init", this::init);

        commands.put("add", this::add);

        commands.put("commit", this::commit);

        commands.put("rm", this::remove);

        commands.put("log", this::log);

        commands.put("global-log", this::globalLog);

        commands.put("find", this::find);

        commands.put("status", this::status);

        commands.put("checkout", this::checkout);

        commands.put("branch", this::branch);

        commands.put("rm-branch", this::removeBranch);

        commands.put("reset", this::reset);

        commands.put("merge", this::merge);
    }

    /**
     * Creates a new Gitlet version-control system in the current directory.
     *
     * @param str takes in string
     */

    public void init(String... str) {

        if (str.length != 2) {

            throw new GitletException("Incorrect operands.");
        }

        File gitletDirectory = new File(".gitlet");

        File lgcomms = new File(".gitlet/logcommits");

        if (gitletDirectory.isDirectory()) {
            throw new GitletException("A Gitlet version-control "
                + "system already exists in the current directory.");
        }

        gitletDirectory.mkdir();

        lgcomms.mkdir();

        Date init = new Date();

        init.setTime(0);

        Commits suur = new Commits("initial commit", init, null);

        String hash = Utils.sha1(Utils.serialize(suur));

        suur.saveCommit(hash);

        File brnc = new File(".gitlet/branches/");

        brnc.mkdir();

        File mstfil = new File(".gitlet/branches/master");

        Utils.writeContents(mstfil, hash);


        File surrfail = new File(".gitlet/head/");
        Utils.writeContents(surrfail, "master");

    }

    /**
     * get the head.
     *
     * @return commit
     */

    public Commits getHead() {

        File refr = new File(".gitlet/head");

        String naam = Utils.readContentsAsString(refr);

        File brn = new File(".gitlet/branches/" + naam);

        String heesh = Utils.readContentsAsString(brn);


        File cmfail = new File(".gitlet/logcommits/" + heesh);

        return Utils.readObject(cmfail, Commits.class);
    }

    /**
     * Adds a copy of the file as it currently exists
     * to the staging area (see the description of the commit command).
     * @param str takes in a sting.
     */
    public void add(String... str) {

        if (str.length != 3) {

            throw new GitletException("Incorrect operands.");
        }

        Commits laysurr = getHead();

        String heesh = getHashedHead();

        laysurr.addToStage(str[2]);


        laysurr.saveCommit(heesh);


    }

    /**
     * Saves a snapshot of certain files in the
     * current commit and staging area so they can be
     * restored at a later time, creating a new commit.
     * @param str takes in a string.
     */

    public void commit(String... str) {

        if (str.length == 2) {

            throw new GitletException("Please enter a commit message.");
        }

        if (str.length != 3) {

            throw new GitletException("Incorrect operands.");
        }





        Commits nayacom = new Commits(str[2], new Date(), getHashedHead());

        clearStage();


        String heesh = Utils.sha1(Utils.serialize(nayacom));

        File surrfail = new File(".gitlet/head");

        String naam = Utils.readContentsAsString(surrfail);

        File brn = new File(".gitlet/branches/" + naam);

        Utils.writeContents(brn, heesh);

        nayacom.saveCommit(heesh);


    }

    /**
     * remove from staging area.
     * @param str takes in a string.
     */

    public void remove(String... str) {

        if (str.length != 3) {

            throw new GitletException("Incorrect operands.");
        }
        Commits getsuur = getHead();

        String heesh = getHashedHead();

        getsuur.removeCommit(str[2]);

        getsuur.saveCommit(heesh);

    }

    /**
     * print the logs of the commits.
     * @param str takes in str.
     */

    public void log(String... str) {

        if (str.length != 2) {

            throw new GitletException("Incorrect operands.");

        }

        String headHash = getHashedHead();

        Commits hcoomm = Commits.getCommit(headHash);

        while (headHash != null) {

            System.out.println("===");

            hcoomm.log(headHash);

            System.out.println();

            headHash = hcoomm.parentRef();

            if (headHash != null) {

                hcoomm = Commits.getCommit(headHash);
            }

        }

    }

    /**
     * Like log, except displays information about all commits ever made.
     * The order of the commits does not matter.
     * @param str takes in sting.
     */

    public void globalLog(String... str) {

        if (str.length != 2) {

            throw new GitletException("Incorrect operands.");
        }
        List<String> hurrcomms = Utils.plainFilenamesIn(".gitlet/logcommits");

        for (String commms : hurrcomms) {

            System.out.println("===");

            Commits.getCommit(commms).log(commms);

            System.out.println();

        }

    }

    /**
     * Prints out the ids of all commits that have
     * the given commit message, one per line.
     * @param str takes in a string
     */

    public void find(String... str) {
        if (str.length == 2) {
            throw new GitletException(
                "Please enter a commit message.");
        }
        if (str.length != 3) {
            throw new GitletException("Incorrect operands.");
        }
        List<String> allCommits = Utils.plainFilenamesIn(".gitlet/logcommits");
        boolean milla = false;
        for (String coms : allCommits) {
            String msgs = Commits.getCommit(coms).logMessage();
            if (str[2].equals(msgs)) {
                System.out.println(coms);
                milla = true;
            }
        }
        if (!milla) {
            throw new GitletException("Found no commit with that message.");
        }
    }
    /**
     * gives you the state of the current.
     * @param str takes in a string
     */

    public void status(String... str) {
        if (str.length != 2) {
            throw new GitletException("Incorrect operands.");
        }
        List<String> brs = Utils.plainFilenamesIn(".gitlet/branches");
        Collections.sort(brs); System.out.println("=== Branches ===");
        for (String brn : brs) {
            File rf = new File(".gitlet/head");
            String name = Utils.readContentsAsString(rf);
            if (brn.equals(name)) {
                System.out.println("*" + brn);
            } else {
                System.out.println(brn);
            }
        }
        System.out.println(); Set<String> si = getHead().getStage().keySet();
        List<String> srt = new ArrayList<>(si);
        Collections.sort(srt); System.out.println("=== Staged Files ===");
        for (String stageNames : srt) {
            System.out.println(stageNames);
        }
        System.out.println();
        List<String> uf = getHead().getUntrackedFiles();
        Collections.sort(uf); System.out.println("=== Removed Files ===");
        for (String hutt : uf) {
            System.out.println(hutt);
        }
        System.out.println();
        Set<String> fset = getHead().getFiles().keySet();
        List<String> sofils = new ArrayList<>(fset); Collections.sort(sofils);
        System.out.println("=== Modifications Not Staged For Commit ===");
        List<String> hh = new ArrayList<>();
        for (String na : srt) {
            File xf = new File(na);
            if (!xf.exists()) {
                hh.add(na + " (deleted)");
            } else {
                String rf = Utils.readContentsAsString(xf);
                String h1 = Utils.sha1(rf);
                if (!h1.equals(getHead().getStage().get(na))) {
                    hh.add(na + " (modified)");
                }
            }
        }
        for (String file : sofils) {
            File rfi = new File(file);
            if (!uf.contains(file) && !rfi.exists()) {
                hh.add(file + " (deleted)");
            }
            if (rfi.exists()) {
                String falcont = Utils.readContentsAsString(rfi);
                String hash = Utils.sha1(falcont);
                if (!si.contains(file)
                    && !hash.equals(getHead().getFiles().get(file))) {
                    hh.add(file + " (modified)");
                }
            }
        }
        Collections.sort(hh);
        for (String s : hh) {
            System.out.println(s);
        }
        System.out.println(); List<String> nd = Utils.plainFilenamesIn(".");
        System.out.println("=== Untracked Files ==="); Collections.sort(nd);
        for (String n1 : nd) {
            if (!si.contains(n1) && !fset.contains(n1)) {
                System.out.println(n1);
            } else if (uf.contains(n1)) {
                System.out.println(n1);
            }
        }
        System.out.println();
    }
    /**
     * Checkout is a kind of general command that can
     * do a few different things depending on what its arguments are.
     * @param str takes in a string
     */

    public void checkout(String... str) {

        if (str.length == 4 && str[2].equals("--")) {

            Commits surr = getHead();

            File fnaam = new File(str[3]);

            if (!surr.getFiles().containsKey(str[3])) {
                throw new
                    GitletException("File does not exist in that commit.");
            }

            String hesh = surr.getFiles().get(str[3]);

            File nayeefaile = new File(".gitlet/" + hesh);

            String newContents = Utils.readContentsAsString(nayeefaile);

            Utils.writeContents(fnaam, newContents);

        } else if (str.length == 5 && str[3].equals("--")) {
            String comid = str[2];
            String firstsaat = comid.substring(0, 7);
            List<String> allCommits
                = Utils.plainFilenamesIn(".gitlet/logcommits");
            String headac = "";
            for (String commitIds : allCommits) {
                if (commitIds.substring(0, 7).equals(firstsaat)) {
                    headac = commitIds;
                }
            }
            Commits layc = Commits.getCommit(headac);
            File naamoffile = new File(str[4]);
            if (!layc.getFiles().containsKey(str[4])) {
                throw new GitletException(
                    "File does not exist in that commit.");
            }
            String heesh = layc.getFiles().get(str[4]);
            File nayefaill = new File(".gitlet/" + heesh);
            String nayacon = Utils.readContentsAsString(nayefaill);
            Utils.writeContents(naamoffile, nayacon);


        } else if (str.length == 3) {
            madudCheck(str);


        } else {
            throw new GitletException("Incorrect operands.");
        }

    }


    /**
     * help checker.
     * @param str takes in string
     */

    public void madudCheck(String... str) {
        File brrns = new File(".gitlet/branches/" + str[2]);
        if (!brrns.exists()) {
            throw new GitletException("No such branch exists.");
        }
        String headName = Utils.readContentsAsString(brrns);

        File headFile = new File(".gitlet/head");
        String name = Utils.readContentsAsString(headFile);

        if (str[2].equals(name)) {
            throw new
                GitletException("No need to checkout the current branch. ");
        }
        Commits head = getHead();
        Commits newCommit = Commits.getCommit(headName);
        List<String> failannn = Utils.plainFilenamesIn(".");

        for (String s : failannn) {

            File nayeefail = new File(s);

            String cont = Utils.readContentsAsString(nayeefail);

            String heesh = Utils.sha1(cont);

            if (!head.getFiles().containsKey(s)) {
                if (newCommit.getFiles().containsKey(s)
                    && !heesh.equals(newCommit.getFiles().get(s))) {
                    throw new
                        GitletException("There is an untracked file"
                        + " in the way; delete it or add it first.");
                }
            }
        }

        for (String s : head.getFiles().keySet()) {

            Utils.restrictedDelete(s);
        }
        for (String ca : newCommit.getFiles().keySet()) {
            String heesh = newCommit.getFiles().get(ca);
            File nayeefail = new File(".gitlet/" + heesh);

            File failan = new File(ca);
            String content = Utils.readContentsAsString(nayeefail);

            Utils.writeContents(failan, content);
        }

        newCommit.getStage().clear();

        newCommit.saveCommit(headName);

        File head2 = new File(".gitlet/head");

        Utils.writeContents(head2, str[2]);
    }


    /**
     * returns branch.
     * @param str takes in a string
     */

    public void branch(String... str) {

        if (str.length != 3) {

            throw new GitletException("Incorrect operands.");
        }
        List<String> brns = Utils.plainFilenamesIn(
            ".gitlet/branches");
        if (
            brns.contains(str[2])) {
            throw new
                GitletException(
                    "A branch with that name already exists.");
        }
        String heesh = getHashedHead();
        File mstfile = new File(".gitlet/branches/" + str[2]);
        Utils.writeContents(mstfile, heesh);
    }

    /**
     * Deletes the branch with the given name.
     * @param str takes in a string
     */

    public void removeBranch(String... str) {
        if (str.length != 3) {

            throw new GitletException("Incorrect operands.");
        }

        List<String> brns = Utils.plainFilenamesIn(".gitlet/branches");
        if (
            !brns.contains(str[2])) {

            throw new
                GitletException(
                    "A branch with that name does not exist.");
        }

        File connection = new File(".gitlet/head");

        String naam = Utils.readContentsAsString(connection);

        if (
            naam.equals(str[2])) {

            throw new GitletException(
                "Cannot remove the current branch.");
        }

        File branch = new File(".gitlet/branches/" + str[2]);
        branch.delete();
    }

    /**
     * Checks out all the files tracked by the given commit.
     * @param str takes in a string
     */


    public void reset(String... str) {
        if (str.length != 3) {

            throw new GitletException("Incorrect operands.");
        }

        Commits laycomm = Commits.getCommit(str[2]);

        Commits surrdoh = getHead();

        List<String> failann = Utils.plainFilenamesIn(".");


        for (String ayee : failann) {

            File nayefailan = new File(ayee);

            String samaan = Utils.readContentsAsString(nayefailan);

            String heesh = Utils.sha1(samaan);

            if (
                !surrdoh.getFiles().containsKey(ayee)) {

                if (laycomm.getFiles().containsKey(ayee)
                    && !heesh.equals(laycomm.getFiles().get(ayee))) {

                    throw new
                        GitletException("There is an untracked file"
                        + " in the way; delete it or add it first.");
                }
            }
        }

        for (
            String failan : laycomm.getFiles().keySet()) {

            String[] inp = {"git", "checkout", str[2], "--", failan};

            checkout(inp);
        }

        laycomm.getStage().clear();

        laycomm.getUntrackedFiles().clear();

        laycomm.saveCommit(str[2]);

        File surrf = new File(".gitlet/head");

        String surrbr = Utils.readContentsAsString(surrf);

        File bfile = new File(".gitlet/branches/" + surrbr);

        Utils.writeContents(bfile, str[2]);


    }


    /**
     * Merges files from the given branch into the current branch.
     * @param str takes in a string
     */
    public void merge(String... str) {
        if (str.length != 3) {
            throw new GitletException("Incorrect operands.");
        }

        List<String> branches = Utils.plainFilenamesIn(".gitlet/branches");
        if (!branches.contains(str[2])) {
            throw new GitletException("A branch with "
                + "that name does not exist.");
        }

        String argBranchName = str[2];

        File argFile = new File(".gitlet/branches/" + argBranchName);
        String argHash = Utils.readContentsAsString(argFile);
        Commits acom = Commits.getCommit(argHash);

        Commits head = getHead();
        if (!head.getStage().isEmpty() || !head.getUntrackedFiles().isEmpty()) {
            throw new GitletException("You have uncommitted changes.");
        }

        boolean foundConflict = false;

        File headFile = new File(".gitlet/head");
        String headBranchName = Utils.readContentsAsString(headFile);
        File headBranch = new File(".gitlet/branches/" + headBranchName);

        if (headBranchName.equals(argBranchName)) {
            throw new GitletException("Cannot merge a branch with itself.");
        }
        createtheMerge(acom);

        String headHash = getHashedHead();
        Commits headCommit = Commits.getCommit(headHash);

        createtheMerge(argHash);

        Commits split = getSplit(argHash);
        if (getHashedHead().equals(Utils.sha1(Utils.serialize(split)))) {
            Utils.writeContents(headBranch, headHash);
            throw new GitletException("Current branch fast-forwarded.");
        }

        for (String file : split.getFiles().keySet()) {
            if (acom.getFiles().keySet().contains(file)
                && headCommit.getFiles().keySet().contains(file)) {
                if (!acom.getFiles()
                    .get(file).equals(split.getFiles().get(file))) {
                    if (headCommit.getFiles()
                        .get(file).equals(split.getFiles().get(file))) {
                        String[] inp = {"git", "checkout", argHash, "--", file};
                        checkout(inp);
                        add("git", "add", file);
                    } else {
                        createtheMerge(file, acom, headCommit);
                        foundConflict = true;
                    }
                }
            }
            if (acom.getFiles().keySet().contains(file)
                && !headCommit.getFiles().keySet().contains(file)) {
                if (!acom
                    .getFiles().get(file).equals(split.getFiles().get(file))) {
                    createtheMerge(file, acom, headCommit);
                    foundConflict = true;
                }
            }
            if (!acom.getFiles().keySet().contains(file)
                && headCommit.getFiles().keySet().contains(file)) {
                if (!headCommit
                    .getFiles().get(file).equals(split.getFiles().get(file))) {
                    createtheMerge(file, acom, headCommit);
                    foundConflict = true;
                }
            }
            if (!acom.getFiles().keySet().contains(file)
                && headCommit.getFiles().keySet().contains(file)) {
                if (headCommit.getFiles()
                    .get(file).equals(split.getFiles().get(file))) {
                    remove("git", "rm", file);
                }
            }
        }

        for (String file : acom.getFiles().keySet()) {
            if (!split.getFiles().keySet().contains(file)
                && !headCommit.getFiles().keySet().contains(file)) {
                String[] inp = {"git", "checkout", argHash, "--", file};
                checkout(inp);
                add("git", "add", file);
            }
            if (headCommit.getFiles().keySet().contains(file)
                && !split.getFiles().keySet().contains(file)) {
                if (!acom.getFiles().get(file)
                    .equals(headCommit.getFiles().get(file))) {
                    createtheMerge(file, acom, headCommit);
                    foundConflict = true;
                }

            }
        }

        String message =
            "Merged " + argBranchName + " into " + headBranchName + ".";

        Commits newCommit =
            new Commits(message, new Date(), getHashedHead(), argHash);
        String hash = Utils.sha1(Utils.serialize(newCommit));

        File h = new File(".gitlet/head");
        String name = Utils.readContentsAsString(h);
        File branch = new File(".gitlet/branches/" + name);

        Utils.writeContents(branch, hash);
        newCommit.saveCommit(hash);

        if (foundConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }
    
    /**
     * Helper function.
     * Creates mer, arg, and hed commm.
     * @param headCommit hed
     * @param file file
     * @param argCommit takes in arg.
     */

    /**
     * springs Check.
     * @param commitHash commits hash
     */
    public void woheeSpCheck(String commitHash) {

        String heesh = getHashedHead();

        Commits comm = Commits.getCommit(heesh);

        while (heesh != null) {

            if (
                commitHash.equals(heesh)) {

                throw new GitletException(
                    "Given branch is an "
                    + "ancestor of the current branch.");
            }
            heesh = comm.parentRef();

            if (
                heesh != null) {
                comm = Commits.getCommit(heesh);
            }
        }
    }

    /**
     * grabs the split.
     * @param commitHash commits the hash.
     * @return otcom.
     */
    public Commits getSplit(String commitHash) {
        HashSet<String> purana = new HashSet<>();

        String othas = commitHash;

        Commits otcom = Commits.getCommit(othas);

        String heash = getHashedHead();

        Commits homm = Commits.getCommit(heash);

        while (heash != null) {

            heash = homm.parentRef();

            purana.add(heash);

            if (heash != null) {

                homm = Commits.getCommit(heash);
            }
        }
        while (
            othas != null) {
            if (
                purana.contains(othas)) {

                return otcom;
            }
            othas = otcom.parentRef();
            if (othas != null) {

                otcom = Commits.getCommit(othas);
            }
        }
        return otcom;
    }

    /**
     * hashes the head and returns it.
     * @return the branchcontent
     */

    public String getHashedHead() {

        File hashad = new File(
            ".gitlet/head");

        String surrstuff = Utils.readContentsAsString(hashad);

        File brs = new File(
            ".gitlet/branches/" + surrstuff);

        String brscont = Utils.readContentsAsString(brs);

        return brscont;
    }


    /**
     * Checks the untracked C.
     * @param c reeturrns it.
     */

    public void untCheck(Commits c) {
        Commits headhas = getHead();

        List<String> failan = Utils.plainFilenamesIn(".");

        for (String s : failan) {

            File nayeefail = new File(s);

            String cns = Utils.readContentsAsString(nayeefail);

            String surrhash = Utils.sha1(cns);

            if (!headhas.getFiles().containsKey(s)) {

                if (
                    c.getFiles().containsKey(s)
                    && !surrhash.equals(c.getFiles().get(s))) {
                    throw new GitletException("There is "
                        + "an untracked file in the way; "
                        + "delete it or add it first.");
                }
            }
        }
    }

    /**
     * Clears up the stage.
     */

    public void clearStage() {

        Commits headhash = getHead();

        headhash.getStage().clear();

        headhash.getUntrackedFiles().clear();

        headhash.saveCommit(getHashedHead());
    }
    /**
     * Clears up the stage.
     * @param argCommit arg
     * @param file fil
     * @param headCommit hcomm
     */
    public void mergeConflict (String file, Commits argCommit,
                               Commits headCommit) {
        File cu = new File(".gitlet/" + headCommit.getFiles().get(file));
        File argument = new File(".gitlet/" + argCommit.getFiles().get(file));
        if (!cu.exists()) {
            Utils.writeContents(cu, "");
        }
        if (!argument.exists()) {
            Utils.writeContents(argument, "");
        }
        File act = new File(file);
        String cCont = Utils.readContentsAsString(cu);
        String argCont = Utils.readContentsAsString(argument);
        Utils.writeContents(act,
            "<<<<<<< HEAD\n", cCont, "=======\n", argCont, ">>>>>>>\n");
        add("git", "add", file);
    }
    /**
     * parse it through the given the command blood.
     * @param str returns str
     */

    public void parseLine(String... str) {
        if (
            str.length < 1) {
            throw new GitletException(
                "Please enter a command.");
        }
        if (
            !commands.containsKey(str[0])) {
            throw new GitletException("No command with that name exists.");
        }
        File report = new File(".gitlet");
        if (
            !str[0].equals("init") && !report.exists()) {
            throw new GitletException("Not in an initialized "
                + "Gitlet directory.");
        }
        String[] newInputList = new String[str.length + 1];

        newInputList[0] = "git";

        System.arraycopy(str, 0, newInputList, 1, str.length);

        commands.get(newInputList[1]).accept(newInputList);
    }
}
