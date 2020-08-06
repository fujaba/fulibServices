package javaPackagesToJavaDoc;

import javaPackagesToJavaDoc.JavaDoc.DocFile;
import javaPackagesToJavaDoc.JavaDoc.Folder;
import javaPackagesToJavaDoc.JavaDoc.HaveContent;
import javaPackagesToJavaDoc.JavaPackages.*;
import javaPackagesToJavaDoc.JavaDoc.JavaDocEditor;
import org.fulib.FulibTools;
import org.fulib.servicegenerator.FulibPatternDiagram;
import org.fulib.tables.ObjectTable;
import org.fulib.yaml.Yaml;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings( {"unchecked", "deprecation"} )
public class TestPackageToDoc
{
   @Test
   public void testOneWaySync()
   {
      JavaPackagesEditor javaPackagesEditor = new JavaPackagesEditor();

      ModelCommand cmd = new HaveRoot().setId("root");
      javaPackagesEditor.execute(cmd);

      cmd = new HaveSubUnit().setParent("root").setId("sub");
      javaPackagesEditor.execute(cmd);

      cmd = new HaveLeaf().setParent("sub").setVTag("1.0").setId("c");
      javaPackagesEditor.execute(cmd);

      Object root = javaPackagesEditor.getModelObject("root");
      assertThat(root, notNullValue());

      FulibTools.objectDiagrams().dumpSVG("tmp/OneWaySyncStart.svg",
            root,
            javaPackagesEditor.getActiveCommands().values());

      String yaml = Yaml.encode(javaPackagesEditor.getActiveCommands().values());

      JavaDocEditor javaDocEditor = new JavaDocEditor();
      javaDocEditor.loadYaml(yaml);

      Folder rootFolder = (Folder) javaDocEditor.getModelObject("root");
      assertThat(rootFolder, notNullValue());

      FulibTools.objectDiagrams().dumpSVG("tmp/OneWaySyncFirstForward.svg",
            javaDocEditor.getActiveCommands().values(),
            rootFolder);

      cmd = new HaveRoot().setId("nRoot");
      javaPackagesEditor.execute(cmd);

      cmd = new HaveSubUnit().setParent("nRoot").setId("root");
      javaPackagesEditor.execute(cmd);

      cmd = new HaveRoot().setId("sub");
      javaPackagesEditor.execute(cmd);

      cmd = new HaveLeaf().setParent("sub").setVTag("1.1").setId("c2");
      javaPackagesEditor.execute(cmd);

      FulibTools.objectDiagrams().dumpSVG("tmp/OneWaySyncTwoRoots.svg",
            javaPackagesEditor.getMapOfModelObjects().values(),
            javaPackagesEditor.getActiveCommands().values());

      yaml = Yaml.encode(javaPackagesEditor.getActiveCommands().values());
      javaDocEditor.loadYaml(yaml);

      FulibTools.objectDiagrams().dumpSVG("tmp/OneWaySyncTwoRootsForward.svg",
            javaDocEditor.getActiveCommands().values(),
            javaDocEditor.getMapOfModelObjects().values());
   }

   @Test
   public void testFirstForwardExample()
   {
      JavaPackagesEditor javaPackagesEditor = new JavaPackagesEditor();

      // create package model
      ModelCommand cmd = new HaveRoot().setId("root");
      javaPackagesEditor.execute(cmd);

      cmd = new HaveSubUnit().setParent("root").setId("sub");
      javaPackagesEditor.execute(cmd);

      cmd = new HaveSubUnit().setParent("sub").setId("leaf");
      javaPackagesEditor.execute(cmd);

      cmd = new HaveLeaf().setParent("leaf").setVTag("1.0").setId("c");
      javaPackagesEditor.execute(cmd);

      Object root = javaPackagesEditor.getModelObject("root");
      assertThat(root, notNullValue());
      JavaPackage sub = (JavaPackage) javaPackagesEditor.getModelObject("sub");
      assertThat(sub, notNullValue());

      FulibTools.objectDiagrams().dumpSVG("tmp/JavaPackagesScenarioStart.svg",
            root,
            javaPackagesEditor.getActiveCommands().values());

      ObjectTable objectTable = new ObjectTable("root", root)
            .expandLink("sub", JavaPackage.PROPERTY_subPackages)
            .expandLink("leaf", JavaPackage.PROPERTY_subPackages)
            .expandLink("c", JavaPackage.PROPERTY_classes);
      assertThat(objectTable.getTable().size(), is(1));

      // forward to doc model
      JavaDocEditor javaDocEditor = new JavaDocEditor();
      String yaml = Yaml.encode(javaPackagesEditor.getActiveCommands().values());
      javaDocEditor.loadYaml(yaml);

      // add some docu content
      javaPackagesToJavaDoc.JavaDoc.ModelCommand docCmd = new HaveContent().setContent("sub docu").setId("subDoc");
      javaDocEditor.execute(docCmd);
      docCmd = new HaveContent().setContent("leaf docu").setId("leafDoc");
      javaDocEditor.execute(docCmd);
      docCmd = new HaveContent().setContent("c docu").setId("c");
      javaDocEditor.execute(docCmd);
      DocFile leafDoc = (DocFile) javaDocEditor.getModelObject("leafDoc");
      assertThat(leafDoc.getContent(), is("leaf docu"));

      Folder rootFolder = (Folder) javaDocEditor.getModelObject("root");
      assertThat(rootFolder, notNullValue());

      FulibTools.objectDiagrams().dumpSVG("tmp/JavaPackagesScenarioFirstForward.svg",
            javaDocEditor.getActiveCommands().values(),
            rootFolder);

      ObjectTable tableFocusedOnSubColumn = new ObjectTable("root", rootFolder)
            .expandLink("sub", Folder.PROPERTY_subFolders);
      tableFocusedOnSubColumn.expandLink("subDoc", Folder.PROPERTY_files);
      ObjectTable tableFocusedOnLeaf = tableFocusedOnSubColumn.expandLink("leaf", Folder.PROPERTY_subFolders);
      tableFocusedOnLeaf.expandLink("leafDoc", Folder.PROPERTY_files);
      assertThat(tableFocusedOnSubColumn.getTable().size(), is(2));
      assertThat(javaDocEditor.getActiveCommands().size(), is(javaPackagesEditor.getActiveCommands().size() + 2));

      // some editing on packages
      assertThat(sub.getUp(), notNullValue());
      ArrayList<ModelCommand> newCommands = new ArrayList<>();

      cmd = new HaveRoot().setId("nRoot");
      javaPackagesEditor.execute(cmd);
      newCommands.add(cmd);

      cmd = new HaveSubUnit().setParent("nRoot").setId("root");
      javaPackagesEditor.execute(cmd);
      newCommands.add(cmd);

      cmd = new HaveLeaf().setParent("sub").setVTag("1.1").setId("c2");
      javaPackagesEditor.execute(cmd);
      newCommands.add(cmd);

      cmd = new HaveLeaf().setParent("leaf").setVTag("1.1").setId("c");
      javaPackagesEditor.execute(cmd);
      newCommands.add(cmd);

      cmd = new HaveRoot().setId("sub");
      javaPackagesEditor.execute(cmd);
      newCommands.add(cmd);

      assertThat(sub.getUp(), nullValue());

      FulibTools.objectDiagrams().dumpSVG("tmp/JavaPackagesSecondRoot.svg",
            javaPackagesEditor.getMapOfModelObjects().values(),
            javaPackagesEditor.getActiveCommands().values());

      // meanwhile at the JavaDocs
      ArrayList<javaPackagesToJavaDoc.JavaDoc.ModelCommand> newDocCommands = new ArrayList<>();
      docCmd = new javaPackagesToJavaDoc.JavaDoc.HaveLeaf().setParent("leaf").setVTag("1.2").setId("c3");
      javaDocEditor.execute(docCmd);
      newDocCommands.add(docCmd);

      docCmd = new javaPackagesToJavaDoc.JavaDoc.HaveLeaf().setParent("leaf").setVTag("1.2").setId("c");
      javaDocEditor.execute(docCmd);
      newDocCommands.add(docCmd);

      FulibTools.objectDiagrams().dumpSVG("tmp/JavaPackagesJavaDocAtVersion1.2.svg",
            javaDocEditor.getActiveCommands().values(),
            javaDocEditor.getMapOfModelObjects().values());

      // sync forward
      yaml = Yaml.encode(newCommands);
      javaDocEditor.loadYaml(yaml);
      leafDoc = (DocFile) javaDocEditor.getModelObject("leafDoc");
      assertThat(leafDoc.getContent(), is("leaf docu"));

      FulibTools.objectDiagrams().dumpSVG("tmp/JavaPackagesSecondRootForward.svg",
            javaDocEditor.getActiveCommands().values(),
            javaDocEditor.getMapOfModelObjects().values());

      // sync backward
      yaml = Yaml.encode(newDocCommands);
      javaPackagesEditor.loadYaml(yaml);

      FulibTools.objectDiagrams().dumpSVG("tmp/JavaPackagesSyncVersionBackward.svg",
            javaPackagesEditor.getMapOfModelObjects().values(),
            javaPackagesEditor.getActiveCommands().values());
   }
}
