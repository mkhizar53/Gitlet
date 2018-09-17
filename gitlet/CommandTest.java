package gitlet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.Assert.*;

public class CommandTest {
    private ByteArrayOutputStream output = new ByteArrayOutputStream();
    public String filename = "testFile.txt";
    String[] gitInit = {"git", "init"};
    String[] gitAdd = {"git", "add", filename};
    String[] gitCommit = {"git", "commit", "this is a test"};
    String[] gitCommit2 = {"git", "commit", "this is a test2"};
    String[] gitRm = {"git", "rm", filename};
    String[] gitFind = {"git", "find", "this is a test2"};


    @Before
    public void setUp() throws Exception {
        System.setOut(new PrintStream(output));
        File tf = new File("testFile.txt");
        Utils.writeContents(tf, "hello there");
    }

    @After
    public void tearDown() throws Exception {
        System.setOut(null);

        Utils.restrictedDelete(filename);

        Path jagapay = Paths.get("./.gitlet");

        if (Files.exists(jagapay)) {
            Files.walk(jagapay, FileVisitOption.FOLLOW_LINKS)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    @Test
    public void init() throws Exception {

        Command testInit = new Command();
        testInit.init(gitInit);
        File gt = new File(".gitlet");
        assertEquals(true, gt.exists() && gt.isDirectory());
    }

    @Test
    public void getHead() throws Exception {
        Command agayjaa = new Command();
        agayjaa.init(gitInit);

        Commits surr = agayjaa.getHead();

        assertEquals("initial commit", surr.logMessage());

    }

    @Test
    public void add() throws Exception {
        Command ta = new Command();
        ta.init(gitInit);
        ta.add(gitAdd);

        Commits head = ta.getHead();

        assertEquals(true, head.getStage().containsKey(filename));

        File cmfl = new File(filename);

        String cts = Utils.readContentsAsString(cmfl);

        String heesh = Utils.sha1(cts);

        assertEquals(true, head.getStage().containsKey(filename));
    }

    @Test
    public void commit() throws Exception {
        Command comtest = new Command();
        comtest.init(gitInit);

        comtest.add(gitAdd);

        comtest.commit(gitCommit);

        Commits surr = comtest.getHead();

        assertEquals("this is a test", surr.logMessage());
    }

    @Test
    public void remove() throws Exception {
        Command testRemove = new Command();
        testRemove.init(gitInit);

        testRemove.add(gitAdd);

        testRemove.remove(gitRm);

        Commits surr = testRemove.getHead();
        assertEquals(true, surr.getStage().isEmpty());


        testRemove.add(gitAdd);

        testRemove.commit(gitCommit);

        testRemove.remove(gitRm);


        surr = testRemove.getHead();

        assertEquals(true, surr.getStage().isEmpty());

        assertEquals(true, !surr.getFiles().isEmpty());
        File existsFile = new File(filename);

        assertEquals(true, !existsFile.exists());
        testRemove.commit(gitCommit);

        surr = testRemove.getHead();

        assertEquals(true, surr.getFiles().isEmpty());
    }

    @Test
    public void find() throws Exception {
        Command testFind = new Command();
        testFind.init(gitInit);
        testFind.add(gitAdd);
        testFind.commit(gitCommit);
        testFind.remove(gitRm);
        testFind.commit(gitCommit2);
        testFind.find(gitFind);
        Commits head = testFind.getHead();
        String hash = Utils.sha1(Utils.serialize(head));
        assertEquals(hash + "\n", output.toString());
    }

    @Test
    public void merge() throws Exception {
        Command c = new Command();
        c.init("git", "init");
        String f1 = "f.txt";
        String fileTwo = "g.txt";


        File filees = new File(f1);
        Utils.writeContents(filees, "f content");

        File two = new File(fileTwo);
        Utils.writeContents(two, "two content");


        c.parseLine("add", f1);
        
        c.parseLine("add", fileTwo);
        
        c.parseLine("commit", "two files");

        c.parseLine("branch", "newBranch");

        File f3 = new File("h.txt");
        
        Utils.writeContents(f3, "h content");


        c.parseLine("add", "h.txt");
        
        c.parseLine("rm", f1);
        
        c.parseLine("commit", "added h removed f1");

        c.parseLine("checkout", "newBranch");
        c.parseLine("rm", fileTwo);

        File four = new File("k.txt");
        Utils.writeContents(four, "k content");

        c.parseLine("add", "k.txt");
        c.parseLine("commit", "added k removed filetwo");

        c.parseLine("checkout", "master");
        
        c.parseLine("merge", "newBranch");

        assertEquals(false, filees.exists());
        
        assertEquals(false, two.exists());
        
        assertEquals(true, f3.exists());
        
        assertEquals(true, four.exists());

        filees.delete();
        two.delete();
        f3.delete();
        four.delete();

    }

    @Test
    public void mergeConflict() throws Exception {
        Command c = new Command();
        c.init("git", "init");
        String f1 = "f.txt";
        String fileTwo = "g.txt";


        File f = new File(f1);
        Utils.writeContents(f, "f content");

        File two = new File(fileTwo);
        Utils.writeContents(two, "two content");


        c.parseLine("add", f1);
        c.parseLine("add", fileTwo);
        c.parseLine("commit", "two files");

        c.parseLine("branch", "newBranch");

        File f3 = new File("h.txt");
        Utils.writeContents(f3, "h content");


        c.parseLine("add", "h.txt");
        c.parseLine("rm", f1);
        c.parseLine("commit", "added h removed f1");

        c.parseLine("checkout", "newBranch");
        c.parseLine("rm", fileTwo);

        File four = new File("k.txt");
        Utils.writeContents(four, "k content");

        c.parseLine("add", "k.txt");
        c.parseLine("commit", "added k removed filetwo");

        c.parseLine("checkout", "master");
        c.parseLine("merge", "newBranch");

        assertEquals(false, f.exists());
        assertEquals(false, two.exists());
        assertEquals(true, f3.exists());
        assertEquals(true, four.exists());

        f.delete();

        two.delete();


        f3.delete();

        four.delete();

    }


}
