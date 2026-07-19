package sancho.view.preferences;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Text;
import sancho.model.mldonkey.Option;
import sancho.model.mldonkey.enums.EnumTagType;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

public class MLDonkeyPreferencePage extends FieldEditorPreferencePage {
   private static final int inputFieldLength = 20;

   // JFace 3.2 FieldEditorPreferencePage exposed clearEditors(); it is gone in
   // modern JFace, so reset the private `fields` list reflectively to let this
   // page rebuild its editors dynamically.
   private void clearEditors() {
      try {
         java.lang.reflect.Field field = FieldEditorPreferencePage.class.getDeclaredField("fields");
         field.setAccessible(true);
         List list = (List)field.get(this);
         if (list != null) {
            list.clear();
         }
      } catch (Exception exception) {
         // ignore if the JFace field layout changed
      }
   }

   // Localize the shared Restore-Defaults / Apply buttons on the core-option pages too.
   // This is a FieldEditorPreferencePage, so it doesn't inherit CPreferencePage's relabel;
   // without this, only the "sancho:" pages showed translated buttons.
   public void createControl(Composite parent) {
      super.createControl(parent);
      CPreferencePage.localizeDialogButtons(this.getDefaultsButton(), this.getApplyButton());
   }

   private static int extentX = -1;
   private boolean empty;
   private boolean allOptions;
   private List options = new ArrayList();
   private List filteredOptions = new ArrayList();
   private Composite group;
   private ScrolledComposite sc;
   private Composite myComp;
   private boolean wasEmpty = true;
   private boolean fChanged = false;
   private Pattern regex = null;

   protected MLDonkeyPreferencePage(String title, int style) {
      super(title, style);
   }

   public void setAllOptions() {
      this.allOptions = true;
   }

   public void addOption(Option option) {
      this.options.add(option);
   }

   protected void contributeButtons(Composite composite) {
      composite.setLayoutData(new GridData(768));
      ((GridLayout)composite.getLayout()).numColumns++;
      Label label = new Label(composite, 0);
      label.setLayoutData(new GridData(768));
      label.setText(SResources.getString("p.mouseOverHelp"));
   }

   public void reFilter() {
      this.filteredOptions.clear();

      for (Object optionObject : this.options) { Option option = (Option)optionObject;
         if (this.fChanged) {
            if (!option.getDefaultValue().equals(option.getValue()) && (this.regex == null || this.regex.matcher(option.getName()).find())) {
               this.filteredOptions.add(option);
            }
         } else if (this.regex == null || this.regex.matcher(option.getName()).find()) {
            this.filteredOptions.add(option);
         }
      }
   }

   public void updateRefine(KeyEvent event) {
      Text text = (Text)event.widget;
      switch (event.keyCode) {
         case 8:
            if (this.wasEmpty && text.getText().equals("")) {
               return;
            } else if (text.getText().equals("")) {
               this.wasEmpty = true;
            }
         default:
            this.wasEmpty = false;

            try {
               this.regex = Pattern.compile(text.getText(), Pattern.CASE_INSENSITIVE);
            } catch (PatternSyntaxException invalidPattern) {
            }

            if (this.regex == null) {
               return;
            } else {
               this.group.setRedraw(false);
               this.filteredOptions.clear();
               this.clearEditors();
               String filterText = text.getText();
               Control[] children = this.myComp.getChildren();

               for (int i = children.length - 1; i >= 0; i--) {
                  children[i].dispose();
               }

               boolean isEmpty = filterText == null || filterText.equals("");
               if (isEmpty) {
                  this.regex = null;
               }

               this.reFilter();
               this.createFieldEditors();
               this.sc.setMinSize(this.myComp.computeSize(-1, -1, true));
               this.group.setRedraw(true);
               this.myComp.layout(true);
            }
         case 13:
         case 16777217:
         case 16777218:
         case 16777219:
         case 16777220:
         case 16777221:
         case 16777222:
         case 16777296:
      }
   }

   protected Control createContents(Composite parent) {
      Collections.sort(this.options, new OptionsComparator());
      this.filteredOptions = new ArrayList(this.options.size());
      Iterator iterator = this.options.iterator();

      while (iterator.hasNext()) {
         this.filteredOptions.add(iterator.next());
      }

      if (this.allOptions) {
         Composite refineComposite = new Composite(parent, 0);
         refineComposite.setLayout(WidgetFactory.createGridLayout(3, 5, 5, 5, 5, false));
         refineComposite.setLayoutData(new GridData(768));
         Label label = new Label(refineComposite, 0);
         label.setText(SResources.getString("ti.refine"));
         Text text = new Text(refineComposite, 2052);
         text.setLayoutData(new GridData(768));
         if (SWT.getPlatform().equals("fox")) {
            text.addKeyListener(new KeyAdapter() {
               public void keyPressed(KeyEvent event) {
                  MLDonkeyPreferencePage.this.updateRefine(event);
               }
            });
         } else {
            text.addKeyListener(new KeyAdapter() {
               public void keyReleased(KeyEvent event) {
                  MLDonkeyPreferencePage.this.updateRefine(event);
               }
            });
         }

         Button button = new Button(refineComposite, 32);
         button.setText(SResources.getString("l.changed"));
         button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
               Button button = (Button)event.widget;
               MLDonkeyPreferencePage.this.fChanged = button.getSelection();
               MLDonkeyPreferencePage.this.group.setRedraw(false);
               MLDonkeyPreferencePage.this.filteredOptions.clear();
               MLDonkeyPreferencePage.this.clearEditors();
               Control[] children = MLDonkeyPreferencePage.this.myComp.getChildren();

               for (int i = children.length - 1; i >= 0; i--) {
                  children[i].dispose();
               }

               MLDonkeyPreferencePage.this.reFilter();
               MLDonkeyPreferencePage.this.createFieldEditors();
               MLDonkeyPreferencePage.this.sc.setMinSize(MLDonkeyPreferencePage.this.myComp.computeSize(-1, -1, true));
               MLDonkeyPreferencePage.this.group.setRedraw(true);
               MLDonkeyPreferencePage.this.myComp.layout(true);
            }
         });
      }

      Composite contents;
      if (this.empty) {
         contents = (Composite)super.createContents(parent);
         contents.setLayoutData(new GridData(1808));
         Label label = new Label(contents, 0);
         label.setText(SResources.getString("p.empty"));
         GridData gridData = new GridData(1808);
         gridData.verticalAlignment = 2;
         gridData.horizontalAlignment = 2;
         label.setLayoutData(gridData);
         contents.layout();
      } else {
         this.group = new Composite(parent, 2048);
         GridLayout gridLayout = WidgetFactory.createGridLayout(1, 5, 5, 5, 5, false);
         this.group.setLayout(gridLayout);
         this.group.setLayoutData(new GridData(1808));
         this.sc = new ScrolledComposite(this.group, 768) {
            public Point computeSize(int wHint, int hHint, boolean changed) {
               return new Point(-1, -1);
            }
         };
         this.sc.setLayoutData(new GridData(1808));
         this.sc.setLayout(new FillLayout());
         this.sc.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent event) {
               ScrolledComposite scrolledComposite = (ScrolledComposite)event.widget;
               int height = scrolledComposite.getClientArea().height;
               if (height > 10) {
                  height -= 10;
               }

               ScrollBar scrollBar = scrolledComposite.getVerticalBar();
               if (scrollBar != null) {
                  scrollBar.setIncrement(15);
                  scrollBar.setPageIncrement(height);
               }
            }
         });
         contents = (Composite)super.createContents(this.sc);
         contents.setLayoutData(new GridData(1808));
         this.sc.setExpandHorizontal(true);
         this.sc.setExpandVertical(true);
         this.sc.setContent(contents);
         Point point = contents.computeSize(-1, -1);
         this.sc.setMinSize(point);
         contents.layout();
      }

      this.myComp = contents;
      return contents;
   }

   protected void createFieldEditors() {
      Iterator iterator = this.filteredOptions.iterator();

      while (iterator.hasNext()) {
         Composite parent = this.getFieldEditorParent();
         Option option = (Option)iterator.next();
         String name = option.getName();
         String description = option.getDescription();
         EnumTagType type = option.getType();
         String value = option.getValue();
         String defaultValue = option.getDefaultValue();
         if (description.equals("")) {
            description = option.getName();
         }

         if (defaultValue != null && !defaultValue.equals("")) {
            description = description + "\n(" + SResources.getString("b.default") + ": " + defaultValue + ")";
         }

         if (type == EnumTagType.BOOL || this.isBoolean(value)) {
            this.setupEditor(parent, new BooleanFieldEditor(name, name, 1, parent), description);
         } else if (type != EnumTagType.INT && !this.isInteger(value)) {
            this.setupEditor(parent, new StringFieldEditor(name, name, 20, parent), description);
         } else {
            IntegerFieldEditor editor = new IntegerFieldEditor(name, name, parent) {
               protected void doFillIntoGrid(Composite parent, int numColumns) {
                  this.getLabelControl(parent);
                  Text text = this.getTextControl(parent);
                  GridData gridData = new GridData();
                  gridData.horizontalSpan = numColumns - 1;
                  if (extentX < 0) {
                     GC gc = new GC(text);
                     Point point = gc.textExtent("X");
                     gc.dispose();
                     extentX = point.x;
                  }

                  gridData.widthHint = 20 * extentX;
                  text.setLayoutData(gridData);
               }
            };
            editor.setValidRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
            this.setupEditor(parent, editor, description);
         }
      }
   }

   private boolean isBoolean(String value) {
      return value != null && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"));
   }

   private boolean isInteger(String value) {
      try {
         int number = Integer.parseInt(value);
         return number >= Integer.MIN_VALUE && number <= Integer.MAX_VALUE;
      } catch (NumberFormatException notANumber) {
         return false;
      }
   }

   public void setEmpty(boolean empty) {
      this.empty = empty;
   }

   private void setupEditor(Composite parent, FieldEditor editor, String toolTip) {
      editor.setPage(this);
      editor.setPreferenceStore(this.getPreferenceStore());
      editor.getLabelControl(parent).setToolTipText(toolTip);
      editor.load();
      this.addField(editor);
   }

   // Case-insensitive ordering of options by name for the preference list.
   private static class OptionsComparator implements Comparator {
      public int compare(Object object1, Object object2) {
         Option option1 = (Option)object1;
         Option option2 = (Option)object2;
         return option1.getName().compareToIgnoreCase(option2.getName());
      }
   }
}
