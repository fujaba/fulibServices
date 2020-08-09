package javaPackagesToJavaDoc.JavaPackagesWithPatterns;

import de.hub.mse.ttc2020.solution.M1.Dog;
import org.fulib.servicegenerator.FulibPatternDiagram;

public class HaveRoot extends ModelCommand
{
   private static Pattern pattern = null;

   @Override
   public Pattern havePattern()
   {
      if (pattern == null) {
         pattern = new Pattern();
         PatternObject p = new PatternObject().setPattern(pattern).setPoId("p").setHandleObjectClass(JavaPackage.class).setKind("core");
         new PatternAttribute().setObject(p).setHandleAttrName(JavaPackage.PROPERTY_id).setCommandParamName(ModelCommand.PROPERTY_id);
         new FulibPatternDiagram().dump("tmp/HaveRoot.svg", pattern);
      }

      return pattern;
   }
}