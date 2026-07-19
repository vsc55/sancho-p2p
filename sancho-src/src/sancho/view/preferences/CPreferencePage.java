package sancho.view.preferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

public class CPreferencePage extends PreferencePage {
   protected List editorList = new ArrayList();

   public CPreferencePage(String title) {
      super(title);
   }

   protected Control createContents(Composite parent) {
      return null;
   }

   public void createControl(Composite parent) {
      super.createControl(parent);
      localizeDialogButtons(this.getDefaultsButton(), this.getApplyButton());
   }

   // Relabel the shared Restore-Defaults / Apply buttons from Sancho's translations and
   // widen them to fit. JFace sizes these buttons for its English bundle text, so the
   // longer translated labels (e.g. Spanish "Restaurar valores predeterminados") would
   // otherwise be clipped. Called from every preference-page type — CPreferencePage AND
   // MLDonkeyPreferencePage (a FieldEditorPreferencePage) — so all pages get localized
   // buttons, not just the "sancho:" ones.
   static void localizeDialogButtons(Button defaultsButton, Button applyButton) {
      if (defaultsButton != null) {
         defaultsButton.setText(SResources.getString("b.defaults"));
         fitButtonWidth(defaultsButton);
      }

      if (applyButton != null) {
         applyButton.setText(SResources.getString("b.apply"));
         fitButtonWidth(applyButton);
      }

      Button anyButton = defaultsButton != null ? defaultsButton : applyButton;
      if (anyButton != null) {
         anyButton.getParent().layout(true, true);
      }
   }

   // Grow a button's grid width hint to fit its (possibly longer, translated) label.
   private static void fitButtonWidth(Button button) {
      if (button.getLayoutData() instanceof GridData) {
         GridData gridData = (GridData)button.getLayoutData();
         int preferred = button.computeSize(-1, -1, true).x;
         if (gridData.widthHint < preferred) {
            gridData.widthHint = preferred;
         }
      }
   }

   protected void contributeButtons(Composite buttonBar) {
      buttonBar.setLayoutData(new GridData(768));
      ((GridLayout)buttonBar.getLayout()).numColumns++;
      Label label = new Label(buttonBar, 0);
      label.setLayoutData(new GridData(768));
      label.setText(SResources.getString("p.restart"));
   }

   protected void setupEditor(FieldEditor editor, Composite parent) {
      this.editorList.add(editor);
      if (editor.getNumberOfControls() < 3) {
         editor.fillIntoGrid(parent, 3);
      }

      editor.setPage(this);
      editor.setPreferenceStore(this.getPreferenceStore());
      editor.load();
   }

   protected void setupColorEditor(String name, String labelKey, Composite parent) {
      this.setupEditor(new ColorFieldEditor(name, SResources.getString(labelKey), parent), parent);
   }

   protected void setupFontEditor(String name, String labelKey, Composite parent) {
      this.setupEditor(new FontFieldEditor(name, SResources.getString(labelKey), parent), parent);
   }

   protected BooleanFieldEditor setupBooleanEditor(String name, String labelKey, Composite parent) {
      BooleanFieldEditor editor = new BooleanFieldEditor(name, SResources.getString(labelKey), parent);
      this.setupEditor(editor, parent);
      return editor;
   }

   protected void setupIntegerEditor(String name, String labelKey, int min, int max, Composite parent) {
      IntegerFieldEditor editor = new IntegerFieldEditor(name, SResources.getString(labelKey), parent);
      editor.setValidRange(min, max);
      this.setupEditor(editor, parent);
   }

   protected void setupStringEditor(String name, String labelKey, char echoChar, Composite parent) {
      this.setupStringEditor(name, "", labelKey, echoChar, parent);
   }

   protected void setupStringEditor(String name, String prefix, String labelKey, char echoChar, Composite parent) {
      StringFieldEditor editor = new StringFieldEditor(name, prefix + SResources.getString(labelKey), parent);
      if (echoChar != '0') {
         editor.getTextControl(parent).setEchoChar(echoChar);
      }

      this.setupEditor(editor, parent);
   }

   protected void setupDirectoryEditor(String name, String labelKey, Composite parent) {
      this.setupEditor(new GCJDirectoryFieldEditor(name, SResources.getString(labelKey), parent), parent);
   }

   protected void setupFileEditor(String name, String labelKey, String[] extensions, Composite parent) {
      GCJFileFieldEditor editor = new GCJFileFieldEditor(name, SResources.getString(labelKey), false, parent, true);
      editor.setFileExtensions(extensions);
      this.setupEditor(editor, parent);
   }

   protected void createSeparator(Composite parent) {
      Label label = new Label(parent, 258);
      GridData gridData = new GridData(768);
      gridData.horizontalSpan = 3;
      label.setLayoutData(gridData);
   }

   protected void createInformationLabel(Composite parent, String labelKey) {
      Label label = new Label(parent, 0);
      GridData gridData = new GridData(768);
      gridData.horizontalSpan = 3;
      label.setLayoutData(gridData);
      label.setText(SResources.getString(labelKey));
   }

   protected void setCompositeLayout(Composite composite) {
      composite.setLayout(WidgetFactory.createGridLayout(3, 5, 5, 5, 5, false));
      Point size = composite.computeSize(-1, -1);
      ((ScrolledComposite)composite.getParent()).setMinSize(size);
      composite.layout();
   }

   protected Composite createNewTab(TabFolder tabFolder, String labelKey) {
      return this.createNewTab(tabFolder, labelKey, null);
   }

   protected Composite createNewTab(TabFolder tabFolder, String labelKey, String imageKey) {
      TabItem item = new TabItem(tabFolder, 0);
      Composite composite = new Composite(tabFolder, 0);
      composite.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      composite.setLayoutData(new GridData(1808));
      ScrolledComposite scrolledComposite = new ScrolledComposite(composite, 768) {
         public Point computeSize(int wHint, int hHint, boolean changed) {
            return new Point(-1, -1);
         }
      };
      scrolledComposite.setLayoutData(new GridData(1808));
      scrolledComposite.setLayout(new FillLayout());
      scrolledComposite.addControlListener(new ControlAdapter() {
         public void controlResized(ControlEvent event) {
            ScrolledComposite resizedComposite = (ScrolledComposite)event.widget;
            int height = resizedComposite.getClientArea().height;
            if (height > 10) {
               height -= 10;
            }

            ScrollBar scrollBar = resizedComposite.getVerticalBar();
            if (scrollBar != null) {
               scrollBar.setIncrement(15);
               scrollBar.setPageIncrement(height);
            }
         }
      });
      Composite content = new Composite(scrolledComposite, 0);
      scrolledComposite.setExpandHorizontal(true);
      scrolledComposite.setExpandVertical(true);
      scrolledComposite.setContent(content);
      item.setControl(composite);
      item.setText(SResources.getString(labelKey));
      if (imageKey != null) {
         item.setImage(SResources.getImage(imageKey));
      }

      return content;
   }

   protected void performDefaults() {
      if (this.editorList != null) {
         Iterator iterator = this.editorList.iterator();

         while (iterator.hasNext()) {
            ((FieldEditor)iterator.next()).loadDefault();
         }
      }

      super.performDefaults();
   }

   public boolean performOk() {
      if (this.editorList != null) {
         Iterator iterator = this.editorList.iterator();

         while (iterator.hasNext()) {
            ((FieldEditor)iterator.next()).store();
         }
      }

      return super.performOk();
   }
}
