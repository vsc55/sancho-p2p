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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

public class CPreferencePage extends PreferencePage {
   protected List editorList = new ArrayList();

   public CPreferencePage(String var1) {
      super(var1);
   }

   protected Control createContents(Composite var1) {
      return null;
   }

   public void createControl(Composite parent) {
      super.createControl(parent);
      // JFace labels the Restore-Defaults / Apply buttons from its own English bundle;
      // relabel them from Sancho's translations so the whole dialog follows the locale.
      Button defaultsButton = this.getDefaultsButton();
      if (defaultsButton != null) {
         defaultsButton.setText(SResources.getString("b.defaults"));
      }

      Button applyButton = this.getApplyButton();
      if (applyButton != null) {
         applyButton.setText(SResources.getString("b.apply"));
      }
   }

   protected void contributeButtons(Composite var1) {
      var1.setLayoutData(new GridData(768));
      ((GridLayout)var1.getLayout()).numColumns++;
      Label var2 = new Label(var1, 0);
      var2.setLayoutData(new GridData(768));
      var2.setText(SResources.getString("p.restart"));
   }

   protected void setupEditor(FieldEditor var1, Composite var2) {
      this.editorList.add(var1);
      if (var1.getNumberOfControls() < 3) {
         var1.fillIntoGrid(var2, 3);
      }

      var1.setPage(this);
      var1.setPreferenceStore(this.getPreferenceStore());
      var1.load();
   }

   protected void setupColorEditor(String var1, String var2, Composite var3) {
      this.setupEditor(new ColorFieldEditor(var1, SResources.getString(var2), var3), var3);
   }

   protected void setupFontEditor(String var1, String var2, Composite var3) {
      this.setupEditor(new FontFieldEditor(var1, SResources.getString(var2), var3), var3);
   }

   protected void setupBooleanEditor(String var1, String var2, Composite var3) {
      this.setupEditor(new BooleanFieldEditor(var1, SResources.getString(var2), var3), var3);
   }

   protected void setupIntegerEditor(String var1, String var2, int var3, int var4, Composite var5) {
      IntegerFieldEditor var6 = new IntegerFieldEditor(var1, SResources.getString(var2), var5);
      var6.setValidRange(var3, var4);
      this.setupEditor(var6, var5);
   }

   protected void setupStringEditor(String var1, String var2, char var3, Composite var4) {
      this.setupStringEditor(var1, "", var2, var3, var4);
   }

   protected void setupStringEditor(String var1, String var2, String var3, char var4, Composite var5) {
      StringFieldEditor var6 = new StringFieldEditor(var1, var2 + SResources.getString(var3), var5);
      if (var4 != '0') {
         var6.getTextControl(var5).setEchoChar(var4);
      }

      this.setupEditor(var6, var5);
   }

   protected void setupDirectoryEditor(String var1, String var2, Composite var3) {
      this.setupEditor(new GCJDirectoryFieldEditor(var1, SResources.getString(var2), var3), var3);
   }

   protected void setupFileEditor(String var1, String var2, String[] var3, Composite var4) {
      GCJFileFieldEditor var5 = new GCJFileFieldEditor(var1, SResources.getString(var2), false, var4, true);
      var5.setFileExtensions(var3);
      this.setupEditor(var5, var4);
   }

   protected void createSeparator(Composite var1) {
      Label var2 = new Label(var1, 258);
      GridData var3 = new GridData(768);
      var3.horizontalSpan = 3;
      var2.setLayoutData(var3);
   }

   protected void createInformationLabel(Composite var1, String var2) {
      Label var3 = new Label(var1, 0);
      GridData var4 = new GridData(768);
      var4.horizontalSpan = 3;
      var3.setLayoutData(var4);
      var3.setText(SResources.getString(var2));
   }

   protected void setCompositeLayout(Composite var1) {
      var1.setLayout(WidgetFactory.createGridLayout(3, 5, 5, 5, 5, false));
      Point var2 = var1.computeSize(-1, -1);
      ((ScrolledComposite)var1.getParent()).setMinSize(var2);
      var1.layout();
   }

   protected Composite createNewTab(TabFolder var1, String var2) {
      return this.createNewTab(var1, var2, null);
   }

   protected Composite createNewTab(TabFolder var1, String var2, String var3) {
      TabItem var4 = new TabItem(var1, 0);
      Composite var5 = new Composite(var1, 0);
      var5.setLayout(WidgetFactory.createGridLayout(1, 0, 0, 0, 0, false));
      var5.setLayoutData(new GridData(1808));
      CPreferencePage$1 var6 = new CPreferencePage$1(this, var5, 768);
      var6.setLayoutData(new GridData(1808));
      var6.setLayout(new FillLayout());
      var6.addControlListener(new CPreferencePage$2(this));
      Composite var7 = new Composite(var6, 0);
      var6.setExpandHorizontal(true);
      var6.setExpandVertical(true);
      var6.setContent(var7);
      var4.setControl(var5);
      var4.setText(SResources.getString(var2));
      if (var3 != null) {
         var4.setImage(SResources.getImage(var3));
      }

      return var7;
   }

   protected void performDefaults() {
      if (this.editorList != null) {
         Iterator var1 = this.editorList.iterator();

         while (var1.hasNext()) {
            ((FieldEditor)var1.next()).loadDefault();
         }
      }

      super.performDefaults();
   }

   public boolean performOk() {
      if (this.editorList != null) {
         Iterator var1 = this.editorList.iterator();

         while (var1.hasNext()) {
            ((FieldEditor)var1.next()).store();
         }
      }

      return super.performOk();
   }
}
