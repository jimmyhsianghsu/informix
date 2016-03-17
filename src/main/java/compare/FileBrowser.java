package compare;
import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Comparator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class FileBrowser extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)throws ServletException, IOException {
		res.setContentType("text/html; charset=utf8");
		if("getPage".equals(req.getParameter("action")))res.getWriter().print(getPage("/META-INF/page/fileBrowser.html"));
		else res.getWriter().print(getFiles(req.getParameter("node")));
	}
	private Comparator<File> fileComparator = new Comparator<File>(){
		@Override
		public int compare(File f1,File f2){if(f1.isDirectory())if(f2.isDirectory())return f1.compareTo(f2);else return -1;else if(f2.isDirectory())return 1;else return f1.compareTo(f2);}
	};
	private String getFiles(String node){
		JSONArray jAry = new JSONArray();
		if(node!=null){
			File[] files = null;
			if(node.equals("/"))
				files = File.listRoots();
			else{
				File file = new File(node);
				if(file.exists() && file.isDirectory())files = file.listFiles();
			}
			if(files!=null){
				Arrays.sort(files,fileComparator);
				for(File file : files){
					JSONObject jObj = new JSONObject();
					try{
						jObj.put("id",file.getAbsolutePath());
						jObj.put("text",file.getName().isEmpty()?file.getAbsolutePath():file.getName());
						if(file.isDirectory())jObj.put("state","closed");
					}catch(JSONException je){je.printStackTrace();}
					jAry.put(jObj);
				}
			}
		}
		return jAry.toString();
	}
	private String getPage(String path) {
		BufferedReader br=null;
		CharArrayWriter caw=new CharArrayWriter();
		try {
			br=new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(path), "utf8"));
			char[] c=new char[1024];
			int n=0;
			while((n=br.read(c))!=-1)
				caw.write(c,0,n);
		}catch(UnsupportedEncodingException e){e.printStackTrace();
		}catch(IOException e){e.printStackTrace();
		}finally{if(br!=null)try{br.close();}catch(IOException e){e.printStackTrace();}}
		return new String(caw.toCharArray());
	}
}