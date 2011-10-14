package de.tarent.maven.plugins.pkg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RPMHelperTest {
	Packaging p;
	Packaging.RPMHelper ph;
	TargetConfiguration dc;
	boolean previousfilefound;
	String homedir = System.getProperty("user.home");
	File f = new File(homedir + "/.rpmmacros");
	
	@Before
	public void setUp() throws IOException{		
		p = new Packaging();
		ph = p.new RPMHelper();
		dc = new TargetConfiguration();	
		previousfilefound = false;	
		if(f.exists()){
			FileUtils.moveFile(f, new File(homedir + "/.rpmmacros_Test_backup"));
			previousfilefound = true;
		}
	}
	
	@Test
	public void testCreatingRpmmacrosfileWithoutMaintainerAndRemovingSuccessfully() throws IOException, MojoExecutionException{
		
		ph.setBasePkgDir(new File("/"));		
		ph.createRpmMacrosFile(null, ph, dc);
		Assert.assertTrue("File not found",f.exists());
		ph.restoreRpmMacrosFileBackup(null);
	}
	
	@Test
	public void testCreatingRpmmacrosfileWitMaintainerAndRemovingSuccessfully() throws IOException, MojoExecutionException{
		dc.setMaintainer("Dummy maintainer");		
		ph.setBasePkgDir(new File("/"));		
		ph.createRpmMacrosFile(null, ph, dc);
		Assert.assertTrue(f.exists());
		Assert.assertTrue("String not found", filecontains(f, "%_gpg_name       Dummy maintainer"));
		ph.restoreRpmMacrosFileBackup(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void testCreatingRpmmacrosfileWithoutBaseDirThrowsException() throws IOException, MojoExecutionException{

		ph.createRpmMacrosFile(null, ph, dc);
		
	}
	
	@Test
	public void testPrepareInitialDirectoriesScuccesfully() throws MojoExecutionException{
		File tempRoot = new File("/tmp/BaseTestTemp");
		File base = new File("/tmp/BaseTestTemp/Base");
		ph.setBasePkgDir(base);
		ph.setTempRoot(tempRoot);
		ph.prepareInitialDirectories();
		Assert.assertTrue(new File("/tmp/BaseTestTemp/Base").exists());
		Assert.assertEquals(new File(ph.basePkgDir,"/BUILD"),ph.getBaseBuildDir());
		Assert.assertEquals(new File(ph.basePkgDir,"/SPECS"),ph.getBaseSpecsDir());		
		base.delete();
		
	}
	
	@Test(expected=NullPointerException.class)
	public void testPrepareInitialDirectoriesWithoutBaseOrTempRootThrowsException() throws MojoExecutionException{
		ph.prepareInitialDirectories();
	}
	@Test
	public void testCopyFilesAndSetFileListSuccessfully() throws MojoExecutionException{
		/* TODO: Find a way to test this method
		dc.setSrcAuxFilesDir("/tmp/SrcAuxFilesDir");
		ph.setDstAuxDir(new File("/tmp/DestAuxFilesDir"));
		ph.copyFilesAndSetFileList();
		Assert.assertTrue(new File("/tmp/DestAuxFilesDir/testfile1").exists());
		*/
	}
	
	@Test
	public void testGetVersionReturnsPackagingVersion(){
		Assert.assertEquals(p.version,ph.getVersion());		
	}
	
	@Test
	public void testGetDstArtifactFileReturnsgetBaseBuildDirAndgetTargetArtifactFiletoStringIfNotSet(){
		File testTempdir = new File("/tmp/BaseTestTemp");
		File testArtifactfile = new File("file1");
		ph.setBaseBuildDir(testTempdir);
		ph.setTargetArtifactFile(testArtifactfile);
		Assert.assertEquals(ph.getBaseBuildDir()+"/"+ph.getTargetArtifactFile().toString(),ph.getDstArtifactFile().toString());
		
	}
	
	@After
	public void tearDown() throws IOException{
		
		if(previousfilefound){
			f.delete();
			FileUtils.moveFile(new File(homedir + "/.rpmmacros_Test_backup"),f);
		}
		
	}
	
	private boolean filecontains(File file, String lookup) throws IOException {
		FileInputStream fis = new FileInputStream(file);

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(fis));
			try{
				String currentLine = "";
				while ((currentLine = in.readLine()) != null) {
					if (currentLine.indexOf(lookup) != -1)
						return true;
				}
			}finally{
				IOUtils.closeQuietly(in);
			}
		} finally {
			IOUtils.closeQuietly(fis);		
		}
		return false;
	}
}
