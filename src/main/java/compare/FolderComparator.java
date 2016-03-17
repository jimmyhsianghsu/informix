package compare;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
public class FolderComparator extends HttpServlet{
	private static Comparator<File> fileComparator = new Comparator<File>() {
		@Override
		public int compare(File f1, File f2) {if (f1.isDirectory())if (f2.isDirectory())return f1.compareTo(f2);else return -1;else if (f2.isDirectory())return 1;else return f1.compareTo(f2);}
	};
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res)throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setContentType("text/html; charset=utf8");
		String folder1 = req.getParameter("folder1");
		String folder2 = req.getParameter("folder2");
		System.out.println(folder1+'\n'+folder2);
		if (folder1 != null && folder2 != null) {
			Set<FileEntity> set1 = new LinkedHashSet<FileEntity>();
			Set<FileEntity> set2 = new LinkedHashSet<FileEntity>();
			Set<FileEntity> set3 = new LinkedHashSet<FileEntity>();
			append(new File(folder1), folder1, set1);
			append(new File(folder2), folder2, set2);
			for(FileEntity fe : set1)if(set2.remove(fe))set3.add(fe);
			for(FileEntity fe : set3)set1.remove(fe);
			
			StringBuffer sb1 = new StringBuffer();
			StringBuffer sb2 = new StringBuffer();
			for(FileEntity fe : set1)sb1.append(fe.toString() + '\n');
			for(FileEntity fe : set2)sb2.append(fe.toString() + '\n');
			JSONObject jObj = new JSONObject();
			try {
				jObj.put("file1", sb1.toString());
				jObj.put("file2", sb2.toString());
			}catch(JSONException e){e.printStackTrace();}
			res.getWriter().print(jObj.toString());
		}
	}

	private void append(File parent, String ref, Set<FileEntity> set) {
		if (parent.exists() && parent.isDirectory()) {
			File[] childs = parent.listFiles();
			Arrays.sort(childs, fileComparator);
			for (File child : childs)if (child.isDirectory())append(child, ref, set);else set.add(new FileEntity(child, ref));
		}
	}
	
	private class FileEntity {
		private String path;
		private long length;
		private String ref;
		public FileEntity(File file, String ref) {
			this.path = file.getAbsolutePath();
			this.length = file.length();
			this.ref = ref;
		}
		public long getLength() {return length;}
		public String getPath() {return path;}
		public String getRef() {return ref;}
		@Override
		public boolean equals(Object obj) {
			if (obj != null && obj instanceof FileEntity) {
				FileEntity fe = (FileEntity) obj;
				return path.substring(ref.length()).equals(fe.getPath().substring(fe.getRef().length())) && length == fe.getLength();
			}
			return false;
		}
		@Override
		public int hashCode() {return path.substring(ref.length()).hashCode() * (int) length;}
		@Override
		public String toString() {return path.substring(ref.length());}
	}
}