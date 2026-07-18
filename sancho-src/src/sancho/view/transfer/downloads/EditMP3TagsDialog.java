package sancho.view.transfer.downloads;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import sancho.model.mldonkey.File;
import sancho.utility.VersionInfo;
import sancho.view.utility.BSpinner;
import sancho.view.utility.SResources;
import sancho.view.utility.WidgetFactory;

public class EditMP3TagsDialog extends Dialog {
   private File file;
   private Text artist;
   private Text album;
   private Text title;
   private Text comment;
   private BSpinner trackNumber;
   private Text year;
   private Combo genre;
   private static final String[] genres = new String[]{
      "Blues",
      "Classic Rock",
      "Country",
      "Dance",
      "Disco",
      "Funk",
      "Grunge",
      "Hip-Hop",
      "Jazz",
      "Metal",
      "New Age",
      "Oldies",
      "Other",
      "Pop",
      "R&B",
      "Rap",
      "Reggae",
      "Rock",
      "Techno",
      "Industrial",
      "Alternative",
      "Ska",
      "Death Metal",
      "Pranks",
      "Soundtrack",
      "Euro-Techno",
      "Ambient",
      "Trip-Hop",
      "Vocal",
      "Jazz+Funk",
      "Fusion",
      "Trance",
      "Classical",
      "Instrumental",
      "Acid",
      "House",
      "Game",
      "Sound Clip",
      "Gospel",
      "Noise",
      "Alt. Rock",
      "Bass",
      "Soul",
      "Punk",
      "Space",
      "Meditative",
      "Instrumental Pop",
      "Instrumental Rock",
      "Ethnic",
      "Gothic",
      "Darkwave",
      "Techno-Industrial",
      "Electronic",
      "Pop-Folk",
      "Eurodance",
      "Dream",
      "Southern Rock",
      "Comedy",
      "Cult",
      "Gangsta Rap",
      "Top 40",
      "Christian Rap",
      "Pop/Funk",
      "Jungle",
      "Native American",
      "Cabaret",
      "New Wave",
      "Psychedelic",
      "Rave",
      "Showtunes",
      "Trailer",
      "Lo-Fi",
      "Tribal",
      "Acid Punk",
      "Acid Jazz",
      "Polka",
      "Retro",
      "Musical",
      "Rock & Roll",
      "Hard Rock",
      "Folk",
      "Folk/Rock",
      "National Folk",
      "Swing",
      "Fast-Fusion",
      "Bebob",
      "Latin",
      "Revival",
      "Celtic",
      "Bluegrass",
      "Avantgarde",
      "Gothic Rock",
      "Progressive Rock",
      "Psychedelic Rock",
      "Symphonic Rock",
      "Slow Rock",
      "Big Band",
      "Chorus",
      "Easy Listening",
      "Acoustic",
      "Humour",
      "Speech",
      "Chanson",
      "Opera",
      "Chamber Music",
      "Sonata",
      "Symphony",
      "Booty Bass",
      "Primus",
      "Porn Groove",
      "Satire",
      "Slow Jam",
      "Club",
      "Tango",
      "Samba",
      "Folklore",
      "Ballad",
      "Power Ballad",
      "Rhythmic Soul",
      "Freestyle",
      "Duet",
      "Punk Rock",
      "Drum Solo",
      "A Cappella",
      "Euro-House",
      "Dance Hall",
      "Goa",
      "Drum & Bass",
      "Club-House",
      "Hardcore",
      "Terror",
      "Indie",
      "BritPop",
      "Negerpunk",
      "Polsk Punk",
      "Beat",
      "Christian Gangsta Rap",
      "Heavy Metal",
      "Black Metal",
      "Crossover",
      "Contemporary Christian",
      "Christian Rock",
      "Merengue",
      "Salsa",
      "Thrash Metal",
      "Anime",
      "JPop",
      "Synthpop"
   };

   public EditMP3TagsDialog(Shell var1, File var2) {
      super(var1);
      this.file = var2;
   }

   protected void configureShell(Shell var1) {
      super.configureShell(var1);
      var1.setImage(VersionInfo.getProgramIcon());
      var1.setText(SResources.getString("Edit MP3 Tags"));
   }

   protected Control createDialogArea(Composite var1) {
      Composite var2 = (Composite)super.createDialogArea(var1);
      var2.setLayout(WidgetFactory.createGridLayout(2, 5, 5, 10, 5, false));
      this.createLabel(var2, "Artist:");
      this.artist = this.createText(var2, this.file.getFormat().getMP3Artist());
      this.createLabel(var2, "Album:");
      this.album = this.createText(var2, this.file.getFormat().getMP3Album());
      this.createLabel(var2, "Title:");
      this.title = this.createText(var2, this.file.getFormat().getMP3Title());
      this.createLabel(var2, "Comment:");
      this.comment = this.createText(var2, this.file.getFormat().getMP3Comment());
      this.createLabel(var2, "Track number:");
      this.trackNumber = new BSpinner(var2, 2048);
      this.trackNumber.setLayoutData(new GridData(768));
      this.trackNumber.setSelection(this.file.getFormat().getMP3TrackNum());
      this.createLabel(var2, "Year");
      this.year = this.createText(var2, this.file.getFormat().getMP3Year());
      this.createLabel(var2, "Genre:");
      this.genre = new Combo(var2, 2048);
      this.genre.setLayoutData(new GridData(768));

      for (int var3 = 0; var3 < genres.length; var3++) {
         this.genre.add(genres[var3]);
      }

      this.genre.select(this.file.getFormat().getMP3Genre());
      return var2;
   }

   protected void createLabel(Composite var1, String var2) {
      Label var3 = new Label(var1, 0);
      var3.setText(var2);
      var3.setLayoutData(new GridData(128));
   }

   protected Text createText(Composite var1, String var2) {
      Text var3 = new Text(var1, 2048);
      var3.setLayoutData(new GridData(768));
      var3.setText(var2);
      return var3;
   }

   protected Control createButtonBar(Composite var1) {
      Composite var2 = new Composite(var1, 0);
      var2.setLayoutData(new GridData(768));
      var2.setLayout(WidgetFactory.createGridLayout(2, 5, 5, 5, 0, false));
      Button var3 = new Button(var2, 0);
      var3.setLayoutData(new GridData(768));
      var3.setText(SResources.getString("b.ok"));
      var3.addSelectionListener(new EditMP3TagsDialog$1(this));
      Button var4 = new Button(var2, 0);
      var4.setLayoutData(new GridData(768));
      var4.setText(SResources.getString("b.cancel"));
      var4.addSelectionListener(new EditMP3TagsDialog$2(this));
      // OK is the default button so Enter in the tag fields commits the edits (the
      // hand-rolled button bar set no default, so Enter did nothing).
      var2.getShell().setDefaultButton(var3);
      return var2;
   }

   // $VF: synthetic method
   static File access$000(EditMP3TagsDialog var0) {
      return var0.file;
   }

   // $VF: synthetic method
   static Text access$100(EditMP3TagsDialog var0) {
      return var0.title;
   }

   // $VF: synthetic method
   static Text access$200(EditMP3TagsDialog var0) {
      return var0.artist;
   }

   // $VF: synthetic method
   static Text access$300(EditMP3TagsDialog var0) {
      return var0.album;
   }

   // $VF: synthetic method
   static Text access$400(EditMP3TagsDialog var0) {
      return var0.year;
   }

   // $VF: synthetic method
   static Text access$500(EditMP3TagsDialog var0) {
      return var0.comment;
   }

   // $VF: synthetic method
   static BSpinner access$600(EditMP3TagsDialog var0) {
      return var0.trackNumber;
   }

   // $VF: synthetic method
   static Combo access$700(EditMP3TagsDialog var0) {
      return var0.genre;
   }
}
