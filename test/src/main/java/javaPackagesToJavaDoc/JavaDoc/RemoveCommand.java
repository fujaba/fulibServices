package javaPackagesToJavaDoc.JavaDoc;
import org.fulib.patterns.*;

public class RemoveCommand extends ModelCommand
{

   @Override
   public Object run(JavaDocEditor editor)
   {
      editor.removeModelObject(getId());
      ModelCommand oldCommand = editor.getActiveCommands().get(getId());
      if (oldCommand != null) {
         oldCommand.remove(editor);
      }
      return null;
   }

}
