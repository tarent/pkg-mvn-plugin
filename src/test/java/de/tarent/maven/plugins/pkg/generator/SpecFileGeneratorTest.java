package de.tarent.maven.plugins.pkg.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.tarent.maven.plugins.pkg.AuxFile;

public class SpecFileGeneratorTest {

	SpecFileGenerator specgenerator;
	File spec;
	File dummytestscript;

	@Before
	public void setUp() throws IOException {
		specgenerator = new SpecFileGenerator();
		spec = File.createTempFile("SPECFileGeneratorTest", "out");
		dummytestscript = File.createTempFile("dummytestscript", null);
	}

	@Test
	public void testFileGenerationWithParameters() throws IOException,
			MojoExecutionException {

		specgenerator.setArch("noarch");
		specgenerator.setSummary("Short dummy summary");
		specgenerator.setDependencies("dependency1,dependency2");
		specgenerator
				.setDescription("This is a long description\nwith linebreaks!");
		specgenerator.setGroup("DummyGroup/Tests");
		specgenerator.setUrl("http://dummyurl.com");
		specgenerator.setLicense("GPL");
		specgenerator.setPackageName("dummypackage");
		specgenerator.setVersion("v2");
		specgenerator.setLicense("huhu");
		specgenerator.setRelease("haha");
		specgenerator.generate(spec);

		assertTrue(spec.exists());
	}

	@Test
	public void testCreatePreinstallScriptFromExternalFileSuccessfully()
			throws IOException {
		createDummyTestScript();
		specgenerator.setPreinstallcommandsFromFile(
				dummytestscript.getParentFile(), dummytestscript.getName());
	}

	@Test(expected = IOException.class)
	public void testCreatePreinstallScriptFromExternalFileNotFoundThrowsException()
			throws IOException {
		specgenerator.setPreinstallcommandsFromFile(dummytestscript,
				dummytestscript.getName());
	}

	@Test
	public void testWriteFilesSection() throws MojoExecutionException,
			IOException {

		List<AuxFile> files = new ArrayList<AuxFile>();

		AuxFile testfile1 = new AuxFile("/file1");

		AuxFile testfile2 = new AuxFile("/file2");
		testfile2.setOwner("user");
		testfile2.setGroup("user");
		testfile2.setUserRead(true);
		testfile2.setGroupRead(true);
		testfile2.setOthersRead(true);

		files.add(testfile1);
		files.add(testfile2);

		specgenerator.setFiles(files);
		generateSpecWithMinimumInfo();

		String lookup1 = "%files";
		String lookup2 = "%defattr(755,root,root)";
		String lookup3 = "/file1";
		String lookup4 = "%attr(444,user,user) /file2";

		assertTrue("Could not find string", filecontains(lookup1));
		assertTrue("Could not find string", filecontains(lookup2));
		assertTrue("Could not find string", filecontains(lookup3));
		assertTrue("Could not find string", filecontains(lookup4));
	}

	@Test
	public void testWriteCleanCommandsSection() throws MojoExecutionException,
			IOException {

		String command = "sample clean command";
		List<String> commands = new ArrayList<String>();
		commands.add(command);

		specgenerator.setCleancommands(commands);
		generateSpecWithMinimumInfo();

		String header = "%clean";

		assertEquals(commands, specgenerator.getCleancommands());
		assertTrue(filecontains(header));
		assertTrue(filecontains(command));
	}

	@Test
	public void testWriteBuildCommandsSection() throws MojoExecutionException,
			IOException {

		String command = "sample build command";
		List<String> commands = new ArrayList<String>();
		commands.add(command);

		specgenerator.setBuildcommands(commands);
		generateSpecWithMinimumInfo();

		String header = "%build";

		assertEquals(commands, specgenerator.getBuildcommands());
		assertTrue(filecontains(header));
		assertTrue(filecontains(command));
	}

	@Test
	public void testWriteInstallCommandsSection()
			throws MojoExecutionException, IOException {

		String command = "sample install command";
		List<String> commands = new ArrayList<String>();
		commands.add(command);

		specgenerator.setInstallcommands(commands);
		generateSpecWithMinimumInfo();

		String header = "%install";

		assertEquals(commands, specgenerator.getInstallcommands());
		assertTrue(filecontains(header));
		assertTrue(filecontains(command));
	}

	@Test
	public void testWritePreInstallCommandsSection()
			throws MojoExecutionException, IOException {

		String command = "sample preinstall command";
		List<String> commands = new ArrayList<String>();
		commands.add(command);

		specgenerator.setPreinstallcommands(commands);
		generateSpecWithMinimumInfo();

		String header = "%pre";
		assertEquals(commands, specgenerator.getPreinstallcommands());
		assertTrue(filecontains(header));
		assertTrue(filecontains(command));
	}

	@Test
	public void testWritePostInstallCommandsSection()
			throws MojoExecutionException, IOException {

		String command = "sample postinstall command";
		List<String> commands = new ArrayList<String>();
		commands.add(command);

		specgenerator.setPostinstallcommands(commands);
		generateSpecWithMinimumInfo();

		String header = "%post";

		assertEquals(commands, specgenerator.getPostinstallcommands());
		assertTrue(filecontains(header));
		assertTrue(filecontains(command));
	}

	@Test
	public void testWritePreUnInstallCommandsSection()
			throws MojoExecutionException, IOException {

		String command = "sample preuninstall command";
		List<String> commands = new ArrayList<String>();
		commands.add(command);

		specgenerator.setPreuninstallcommands(commands);
		generateSpecWithMinimumInfo();

		String header = "%preun";

		assertEquals(commands, specgenerator.getPreuninstallcommands());
		assertTrue(filecontains(header));
		assertTrue(filecontains(command));
	}

	@Test
	public void testWritePostUnInstallCommandsSection()
			throws MojoExecutionException, IOException {

		String command = "sample postuninstall command";
		List<String> commands = new ArrayList<String>();
		commands.add(command);

		specgenerator.setPostuninstallcommands(commands);
		generateSpecWithMinimumInfo();

		String header = "%postun";

		assertEquals(commands, specgenerator.getPostuninstallcommands());
		assertTrue(filecontains(header));
		assertTrue(filecontains(command));
	}

	@Test
	public void testSetPreInstallCommandsFromEmptyFileCreatesEmptyArrayList()
			throws IOException {

		specgenerator.setPreinstallcommandsFromFile(
				dummytestscript.getParentFile(), dummytestscript.getName());
		assertEquals(0, specgenerator.getPreinstallcommands().size());
	}

	@Test
	public void testSetPreInstallCommandsFromFileCreatesArrayList()
			throws IOException {

		PrintWriter w = new PrintWriter(dummytestscript);
		w.println("echo 'echo!'");
		w.close();
		specgenerator.setPreinstallcommandsFromFile(
				dummytestscript.getParentFile(), dummytestscript.getName());
		assertEquals(1, specgenerator.getPreinstallcommands().size());
	}

	@Test
	public void testSetPreUninstallCommandsFromEmptyFileCreatesEmptyArrayList()
			throws IOException {

		specgenerator.setPreuninstallcommandsFromFile(
				dummytestscript.getParentFile(), dummytestscript.getName());
		assertEquals(0, specgenerator.getPreuninstallcommands().size());
	}

	@Test
	public void testSetPreUninstallCommandsFromFileCreatesArrayList()
			throws IOException {

		PrintWriter w = new PrintWriter(dummytestscript);
		w.println("echo 'echo!'");
		w.close();
		specgenerator.setPreuninstallcommandsFromFile(
				dummytestscript.getParentFile(), dummytestscript.getName());
		assertEquals(1, specgenerator.getPreuninstallcommands().size());
	}

	@Test
	public void testSetPostInstallCommandsFromEmptyFileCreatesEmptyArrayList()
			throws IOException {

		specgenerator.setPostinstallcommandsFromFile(
				dummytestscript.getParentFile(), dummytestscript.getName());
		assertEquals(0, specgenerator.getPostinstallcommands().size());
	}

	@Test
	public void testSetPostInstallCommandsFromFileCreatesArrayList()
			throws IOException {

		PrintWriter w = new PrintWriter(dummytestscript);
		w.println("echo 'echo!'");
		w.close();
		specgenerator.setPostinstallcommandsFromFile(
				dummytestscript.getParentFile(), dummytestscript.getName());
		assertEquals(1, specgenerator.getPostinstallcommands().size());
	}

	@Test
	public void testSetPostUninstallCommandsFromEmptyFileCreatesEmptyArrayList()
			throws IOException {

		specgenerator.setPostuninstallcommandsFromFile(
				dummytestscript.getParentFile(), dummytestscript.getName());
		assertEquals(0, specgenerator.getPostuninstallcommands().size());
	}

	@Test
	public void testSetPostUninstallCommandsFromFileCreatesArrayList()
			throws IOException {

		PrintWriter w = new PrintWriter(dummytestscript);
		w.println("echo 'echo!'");
		w.close();
		specgenerator.setPostuninstallcommandsFromFile(
				dummytestscript.getParentFile(), dummytestscript.getName());
		assertEquals(1, specgenerator.getPostuninstallcommands().size());
	}

	@Test
	public void testSetPreInstallCommandsFromFileNullCreatesEmptyArrayList()
			throws IOException {

		specgenerator.setPreinstallcommandsFromFile(null, null);
		assertEquals(0, specgenerator.getPreinstallcommands().size());
	}

	@Test
	public void testSetPreUninstallCommandsFromFileNullCreatesEmptyArrayList()
			throws IOException {

		specgenerator.setPreuninstallcommandsFromFile(null, null);
		assertEquals(0, specgenerator.getPreuninstallcommands().size());
	}

	@Test
	public void testSetPostInstallCommandsFromFileNullCreatesEmptyArrayList()
			throws IOException {

		specgenerator.setPostinstallcommandsFromFile(null, null);
		assertEquals(0, specgenerator.getPostinstallcommands().size());
	}

	@Test
	public void testSetPostUninstallCommandsFromFileNullCreatesEmptyArrayList()
			throws IOException {

		specgenerator.setPostuninstallcommandsFromFile(null, null);
		assertEquals(0, specgenerator.getPostuninstallcommands().size());
	}

	@Test
	public void testSetBuildCommandsFromNullCreatesEmptyArrayList()
			throws IOException {

		specgenerator.setBuildcommands(null);
		assertEquals(0, specgenerator.getBuildcommands().size());
	}

	@Test
	public void testSetBuildCommandsFromFileCreatesArrayList()
			throws IOException {

		List<String> setBuildcommands = new ArrayList<String>();
		setBuildcommands.add("test1");
		setBuildcommands.add("test2");
		setBuildcommands.add("test3");
		specgenerator.setBuildcommands(setBuildcommands);
		assertEquals(3, specgenerator.getBuildcommands().size());
	}

	@Test
	public void testSetInstallCommandsFromNullCreatesEmptyArrayList()
			throws IOException {

		specgenerator.setInstallcommands(null);
		assertEquals(0, specgenerator.getInstallcommands().size());
	}

	@Test
	public void testSetInstallCommandsFromArrayList() throws IOException {

		List<String> setInstallCommands = new ArrayList<String>();
		setInstallCommands.add("test1");
		setInstallCommands.add("test2");
		setInstallCommands.add("test3");
		specgenerator.setInstallcommands(setInstallCommands);
		assertEquals(3, specgenerator.getInstallcommands().size());
	}

	@Test
	public void testSetPostInstallCommandsFromNullArrayCreatesEmptyArrayList() {

		specgenerator.setPostinstallcommands(null);
		assertEquals(0, specgenerator.getPostinstallcommands().size());
	}

	@Test
	public void testSetPostUninstallCommandsFromNullArrayCreatesEmptyArrayList() {

		specgenerator.setPostuninstallcommands(null);
		assertEquals(0, specgenerator.getPostuninstallcommands().size());
	}

	@Test
	public void testSetPreInstallCommandsFromNullArrayCreatesEmptyArrayList() {

		specgenerator.setPreinstallcommands(null);
		assertEquals(0, specgenerator.getPreinstallcommands().size());
	}

	@Test
	public void testSetPreUninstallCommandsFromNullArrayCreatesEmptyArrayList() {

		specgenerator.setPreuninstallcommands(null);
		assertEquals(0, specgenerator.getPreuninstallcommands().size());
	}

	@Test
	public void testSetCleanCommandsFromNullArrayCreatesEmptyArrayList() {

		specgenerator.setCleancommands(null);
		assertEquals(0, specgenerator.getCleancommands().size());
	}

	@Test
	public void testSetPrepareCommandsFromArrayList() throws IOException {

		List<String> setPrepareCommands = new ArrayList<String>();
		setPrepareCommands.add("test1");
		setPrepareCommands.add("test2");
		setPrepareCommands.add("test3");
		specgenerator.setPreparecommands(setPrepareCommands);
		assertEquals(3, specgenerator.getPreparecommands().size());
	}

	@Test
	public void testSetPrepareCommandsFromNullArrayCreatesEmptyArrayList() {

		specgenerator.setPreparecommands(null);
		assertEquals(0, specgenerator.getPreparecommands().size());
	}

	@Test
	public void testSetArchDefaultsToNoarchIfNullOrEmpty() {
		specgenerator.setArch(null);
		assertEquals("noarch", specgenerator.getArch());
		specgenerator.setArch("");
		assertEquals("noarch", specgenerator.getArch());
	}

	@Test
	public void testSetGroupDefaultsToUnknownIfNullOrEmpty() {
		specgenerator.setGroup(null);
		assertEquals("unknown", specgenerator.getGroup());
		specgenerator.setGroup("");
		assertEquals("unknown", specgenerator.getGroup());
	}

	@Test
	public void testSetLicenseDefaultsToUnknownIfNullOrEmpty() {
		specgenerator.setLicense(null);
		assertEquals("unknown", specgenerator.getLicense());
		specgenerator.setLicense("");
		assertEquals("unknown", specgenerator.getLicense());
	}

	@Test
	public void testSetReleaseDefaultsTo1IfNullOrEmpty() {
		specgenerator.setRelease(null);
		assertEquals("1", specgenerator.getRelease());
		specgenerator.setRelease("");
		assertEquals("1", specgenerator.getRelease());
	}

	@Test
	public void testSetUrlDefaultsToUnknownDotComIfNullOrEmpty() {
		specgenerator.setUrl(null);
		assertEquals("http://unknown.com", specgenerator.getUrl());
		specgenerator.setUrl("");
		assertEquals("http://unknown.com", specgenerator.getUrl());
	}

	@Test
	public void testSetSummaryDefaultsToUnknownIfNullOrEmpty() {
		specgenerator.setSummary(null);
		assertEquals("unknown", specgenerator.getSummary());
		specgenerator.setSummary("");
		assertEquals("unknown", specgenerator.getSummary());
	}

	@Test
	public void testSetSourceDefaultsToUnknownIfNullOrEmpty() {
		specgenerator.setSource(null);
		assertEquals("unknown", specgenerator.getSource());
		specgenerator.setSource("");
		assertEquals("unknown", specgenerator.getSource());
	}

	@Test
	public void testSetVersionDefaultsToUnknownIfNullOrEmpty() {
		specgenerator.setVersion(null);
		assertEquals("unknown", specgenerator.getVersion());
		specgenerator.setVersion("");
		assertEquals("unknown", specgenerator.getVersion());
	}

	@Test
	public void testSetSuggestsDefaultsToUnknownIfNullOrEmpty() {
		specgenerator.setSuggests(null);
		assertEquals("unknown", specgenerator.getSuggests());
		specgenerator.setSuggests("");
		assertEquals("unknown", specgenerator.getSuggests());
	}

	@Test
	public void testSetDependenciesDefaultsToUnknownIfNullOrEmpty() {
		specgenerator.setDependencies(null);
		assertEquals("unknown", specgenerator.getDependencies());
		specgenerator.setDependencies("");
		assertEquals("unknown", specgenerator.getDependencies());
	}

	@Test
	public void testSetRecommendsDefaultsToUnknownIfNullOrEmpty() {
		specgenerator.setRecommends(null);
		assertEquals("unknown", specgenerator.getRecommends());
		specgenerator.setRecommends("");
		assertEquals("unknown", specgenerator.getRecommends());
	}

	@After
	public void tearDown() {
		spec.delete();
		if (dummytestscript.exists()) {
			dummytestscript.delete();
		}
	}

	private void generateSpecWithMinimumInfo() throws MojoExecutionException,
			IOException {

		specgenerator.setArch("noarch");
		specgenerator.setSummary("Short dummy summary");
		specgenerator.setDependencies("dependency1,dependency2");
		specgenerator
				.setDescription("This is a long description\nwith linebreaks!");
		specgenerator.setGroup("DummyGroup/Tests");
		specgenerator.setUrl("http://dummyurl.com");
		specgenerator.setLicense("GPL");
		specgenerator.setPackageName("dummypackage");
		specgenerator.setVersion("v2");
		specgenerator.setLicense("huhu");
		specgenerator.setRelease("haha");
		specgenerator.generate(spec);
	}

	private boolean filecontains(String lookup) throws IOException {
		FileInputStream fis = new FileInputStream(spec);
		BufferedReader in = new BufferedReader(new InputStreamReader(fis));

		try {
			String currentLine = "";
			while ((currentLine = in.readLine()) != null) {
				if (currentLine.indexOf(lookup) != -1)
					return true;
			}
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(fis);
		}
		return false;
	}

	private void createDummyTestScript() throws FileNotFoundException {
		PrintWriter p = new PrintWriter(dummytestscript);
		p.println("Test command 1");
		p.println("Test command 2");
		p.println("Test command 3");
		p.close();
	}

}
