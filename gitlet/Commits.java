package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Commit class for Gitlet.
 *
 *
 * @author Mohammad Khizar
 */

public class Commits implements Serializable {
    /**
     * A log message for a commit.
     */
    private String _logMessage;
    /**
     * A time for a commit.
     */
    private Date _timeCommit;
    /**
     * Parent of a commit.
     */
    private String _parent;
    /**
     * Hashmap of files for a commit.
     */
    private HashMap<String, String> files;
    /**
     * Stage of a commit.
     */
    private HashMap<String, String> stage;
    /**
     * An array of untracked files.
     */
    private ArrayList<String> untrackedFiles;


    /**
     * A string to account for the other parent reference.
     */
    private String otherParent = null;

    /**
     * Constructor for Commits, LOGMESSAGE, TIMECOMMIT, PARENT.
     */

    public Commits(String logMessage, Date timeCommit, String parent) {
        if (logMessage.equals("")) {
            throw new GitletException("Please enter a commit message.");
        }
        _logMessage = logMessage;
        _timeCommit = timeCommit;
        _parent = parent;
        stage = new HashMap<>();
        files = makeCommit(getP());
        this.untrackedFiles = new ArrayList<>();
    }

    /**
     * This fucntion acts as a second constructor, LOGMESSAGE, TIMECOMMIT,
     * PARENT, PARENTREFERENCE.
     */
    public Commits(String logMessage,
                   Date timeCommit, String parent, String parentReference) {
        this(logMessage, timeCommit, parent);
        otherParent = parentReference;
    }


    /**
     * making commit Par.
     * @param parent takes in par
     * @return files.
     */

    public HashMap<String, String> makeCommit(Commits parent) {
        files = new HashMap<>();
        if (parent == null) {
            return files;
        }
        if (parent.stage.isEmpty() && parent.untrackedFiles.isEmpty()) {
            throw new GitletException("No changes added to the commit.");
        }
        for (String name : parent.files.keySet()) {
            if (!parent.untrackedFiles.contains(name)) {
                files.put(name, parent.files.get(name));
            }
        }
        for (String name : parent.stage.keySet()) {
            files.put(name, parent.stage.get(name));
        }
        return files;
    }

    /**
     *Adding it to the stage.
     * @param name takes in teh name
     */

    public void addToStage(String name) {

        File commitFile = new File(name);
        if (!commitFile.exists()) {
            throw new GitletException("File does not exist.");
        }
        if (untrackedFiles.contains(name)) {
            untrackedFiles.remove(name);
        } else {
            String contents = Utils.readContentsAsString(commitFile);
            String hash = Utils.sha1(contents);
            stage.put(name, hash);

            if (hash.equals(files.get(name))) {
                stage.remove(name);
            } else {
                File stageFile = new File(".gitlet/" + hash);
                Utils.writeContents(stageFile, contents);
            }
        }


    }

    /**
     * Save the commit fam.
     * @param name take in name.
     */

    public void saveCommit(String name) {
        File commitFile = new File(".gitlet/logcommits/" + name);
        Utils.writeObject(commitFile, this);
    }

    /**
     * get the commit NAME.
     * @return commit.
     */

    public static Commits getCommit(String name) {
        if (name.equals(Utils.sha1(Utils.serialize(null)))) {
            return null;
        }
        File commitFile = new File(".gitlet/logcommits/" + name);
        if (!commitFile.exists()) {
            throw new GitletException("No commit with that id exists.");

        }
        return Utils.readObject(commitFile, Commits.class);
    }

    /**
     * remove comm.
     * @param name take in name.
     */

    public void removeCommit(String name) {
        if (!stage.containsKey(name) && !files.containsKey(name)) {
            throw new GitletException("No reason to remove the file.");
        }
        stage.remove(name);
        if (files.containsKey(name)) {
            untrackedFiles.add(name);
            Utils.restrictedDelete(name);
        }
    }

    /**
     * get the parent.
     * @return Commits.
     */

    public Commits getP() {
        if (getParent() != null) {
            return getCommit(_parent);
        }
        return null;
    }


    /**
     * This function will get the parent ref.
     * @return string.
     */
    public String parentRef() {
        if (getParent() != null) {
            return getParent();
        }
        return null;

    }

    /**
     * get the log of the commits, HASH.
     * @param hash take in has
     */

    public void log(String hash) {
        System.out.println(
            "commit " + hash);


        if (getOtherParent() != null) {

            System.out.println(
                "Merge: " + getParent().substring(0, 7)
                + " " + getOtherParent().substring(0, 7));

        }
        SimpleDateFormat time =

            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");


        System.out.println("Date: " + time.format(getTimeCommit()));


        System.out.println(logMessage());
    }


    /**
     * allow the log message to be public.
     * @return STRING
     */

    public String logMessage() {

        return _logMessage;
    }


    /**
     * makes the time commit public.
     *
     * @return DATE
     */
    public Date getTimeCommit() {

        return _timeCommit;
    }


    /**
     * makes the get parent function public.
     * @return STRING
     */
    public String getParent() {

        return _parent;
    }


    /**
     *makes the getfiles STRING, STRING files public.
     * @return HASHMAP
     */
    public HashMap<String, String> getFiles() {

        return files;
    }

    /**
     * makes the getStage STRING, STRING files public.
     * @return HASHMAP
     */

    public HashMap<String, String> getStage() {

        return stage;
    }

    /**
     * makes the untrackedfile STRING public.
     * @return ARRAYLIST
     */

    public ArrayList<String> getUntrackedFiles() {
        return untrackedFiles;
    }

    /**
     * ENABLE otherparent string to be accessed as public.
     * @return STRING.
     */
    public String getOtherParent() {
        return otherParent;
    }
}
