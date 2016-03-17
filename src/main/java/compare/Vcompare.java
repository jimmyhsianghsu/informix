package compare;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.CharArrayWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class Vcompare extends HttpServlet {
	private String mapFile="D:\\0.road1\\2.comp_a1\\mapFile.txt";
	private java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMddHHmm");
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)throws ServletException,IOException {
		if(req.getParameter("map")!=null)mapFile=req.getParameter("map");
		res.setContentType("text/html; charset=utf8");
		String action=req.getParameter("action");
		if("treeFile".equals(action))
			res.getWriter().print(treeFile());
		else if("getMap".equals(action))
			res.getWriter().print(getMap(getServletContext(),mapFile));
		else if ("getPage".equals(action))
			res.getWriter().print(getPage("/META-INF/page/vcompare.html"));
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res)throws ServletException, IOException {
		if(req.getParameter("map")!=null)mapFile=req.getParameter("map");
		req.setCharacterEncoding("UTF-8");
		res.setContentType("text/html; charset=utf8");
		String action=req.getParameter("action");
		String file=req.getParameter("file");
		if("getFile".equals(action))
			res.getWriter().print(getFile(file));
		else if("getFileList".equals(action))
			res.getWriter().print(getFileList(mapFile,file));
		else if("getFile2".equals(action))
			res.getWriter().print(getFile2(mapFile,file,req.getParameter("file1"),req.getParameter("file2")));
		else if("gitFile".equals(action))
			gitFile(mapFile,file);
		else if("writeMap".equals(action))
			writeMap(req.getParameter("maps"));
		else if("pasteFile1".equals(action))
			pasteFile(file,1);
		else if("pasteFile2".equals(action))
			pasteFile(file,2);
		else if("getMatch".equals(action))
			res.getWriter().print(test.RegTest.match(req.getParameter("sb")));
		else if("getFile2f".equals(action))
			res.getWriter().print(getFile2f(req.getParameter("file1"),req.getParameter("file2")));
	}
	private Map<String, List<String>> mapListFile(String txt) {
		Map<String, List<String>> mapList = new LinkedHashMap<String, List<String>>();
		String root = txt.substring(0, txt.lastIndexOf('\\'));
		File folder = new File(root);
		for(String file : folder.list()) {
			if (file.indexOf('@') > -1) {
				String key = file.substring(0, file.indexOf('@'));
				String path = folder.getAbsolutePath() + '\\' + file;
				List<String> list = mapList.get(key);
				if (list == null) {
					list = new ArrayList<String>();
					mapList.put(key, list);
				}
				list.add(path);
			}
		}
		for(String key : mapList.keySet())
			Collections.sort(mapList.get(key), new Comparator<String>() {public int compare(String s1, String s2) {return Integer.valueOf(s1.substring(s1.indexOf('@') + 1, s1.lastIndexOf('.')))-Integer.valueOf(s2.substring(s2.indexOf('@') + 1, s2.lastIndexOf('.')));}});
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(txt), "utf-8"));
			String path = null;
			while ((path = br.readLine()) != null)
				if (!path.isEmpty() && path.indexOf('\\') != -1) {
					String key = path.substring(path.lastIndexOf('\\') + 1);
					List<String> list = mapList.get(key);
					if (list == null) {
						list = new ArrayList<String>();
						mapList.put(key, list);
					}
					list.add(path);
				}
		}catch(UnsupportedEncodingException e){e.printStackTrace();
		}catch(IOException e){e.printStackTrace();
		}finally{if(br != null)try{br.close();}catch(IOException e){e.printStackTrace();}}
		
		for (String key : mapList.keySet()) {
			System.out.println("key=" + key);
			for (String path : mapList.get(key))
				System.out.println("path=" + path);
		}
		return mapList;
	}
	private Map<String,String[]> mapFile(String txt){
		Map<String, List<String>> mapList = mapListFile(txt);
		Map<String, String[]> map = new LinkedHashMap<String, String[]>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(txt), "utf-8"));
			String path = null;
			while ((path = br.readLine()) != null)
				if (!path.isEmpty())
					if (path.indexOf('\\') != -1) {
						String key = path.substring(path.lastIndexOf('\\') + 1);
						List<String> list = mapList.get(key);
						map.put(key,new String[]{list.size()>1?list.get(list.size()-2):"",list.size()>0?list.get(list.size()-1):""});
					} else
						map.put(path.substring(0,path.indexOf('.')),new String[]{path.substring(path.indexOf('.')+1)});
		}catch(UnsupportedEncodingException e){e.printStackTrace();
		}catch(IOException e){e.printStackTrace();
		}finally{if(br != null)try{br.close();}catch(IOException e){e.printStackTrace();}}
		return map;
	}
	private String getFileList(String txt,String file){
		JSONArray jAry = new JSONArray();
		for(String path:mapListFile(txt).get(file))
			jAry.put(path.substring(path.lastIndexOf('\\')+1));
		return jAry.toString();
	}
	private String getFile2(String txt,String file,String file1,String file2){
		String path1=null,path2=null;
		for(String path : mapListFile(txt).get(file)){
			file = path.substring(path.lastIndexOf('\\')+1);
			if(file.equals(file1))path1=path;
			else if(file.equals(file2))path2=path;
		}
		if(path1!=null && path2!=null){
			JSONObject jObj = new JSONObject();
			try {
				jObj.put("file1",compareFile(path1));
				jObj.put("file2",compareFile(path2));
				jObj.put("exist1",new File(path1).exists());
				jObj.put("exist2",new File(path2).exists());
				jObj.put("path1", path1);
				jObj.put("path2", path2);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jObj.toString();
		}
		return null;
	}
	private void gitFile(String txt,String file){
		List<String> list = mapListFile(txt).get(file);
		String file1 = list.get(list.size()-1);
		String file2 = txt.substring(0,txt.lastIndexOf('\\')+1)+file+"@"+sdf.format(new java.util.Date()).substring(2)+file.substring(file.lastIndexOf('.'));
		pasteFile(file1,file2);
		new File(file2).setReadOnly();
	}
	
	private String treeFile(){
		JSONArray jAry = new JSONArray();
		Map<String,String[]> map=mapFile(mapFile);
		JSONObject obj = null;
		JSONArray ary = null;
		String root=null;
		int i=0;
		for(String s:map.keySet()){
			try {
				if(map.get(s).length==1){
					if(obj!=null && ary!=null){
						obj.put("children",ary);
						jAry.put(obj);
					}
					obj=new JSONObject();
					ary=new JSONArray();
					root=s+"["+map.get(s)[0]+"]";
					obj.put("id", i++);
					obj.put("text",root);
					obj.put("iconCls","icon-folder");
				}else if(map.get(s).length==2){
					JSONObject obj1=new JSONObject();
					obj1.put("id",i++);
					obj1.put("text",s);
					String path1=map.get(s)[0];
					String path2=map.get(s)[1];					
					obj1.put("iconCls",exist(path1,path2)?compare(path1,path2)?"icon-ok":"icon-no":"icon-cancel");
					obj1.put("root",root);
					obj1.put("file",s);
					obj1.put("path1",path1);
					obj1.put("path2",path2);
					obj1.put("exist1",new File(path1).exists());
					obj1.put("exist2",new File(path2).exists());
					obj1.put("compare",compare(path1,path2));
					ary.put(obj1);
				}
			} catch (JSONException je) {
				je.printStackTrace();
			}
		}
		return jAry.toString();
	}
	@Deprecated
	private Map<String,String[]> mapFile(ServletContext context,String root){
		Map<String,String[]> map = new LinkedHashMap<String,String[]>();
		BufferedReader br=null;
		try {
			br = new BufferedReader(new InputStreamReader(context.getResourceAsStream("/" + root),"utf-8"));
			String s = null;
			while ((s = br.readLine()) != null)
				if(!s.isEmpty())
					if(s.indexOf('\\')!=-1)
						map.put(s.substring(s.lastIndexOf('\\')+1,s.lastIndexOf('.')).replaceAll("[ \\.\\-\\(\\)]","_"),new String[]{s,br.readLine()});
					else
						map.put(s.substring(0,s.indexOf('.')),new String[]{s.substring(s.indexOf('.')+1)});
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{if(br!=null)try {br.close();} catch (IOException e) {e.printStackTrace();}}
		return map;
	}
	private String getFile(String file){
		Map<String,String[]> map=mapFile(mapFile);
		if(map.get(file)!=null && map.get(file).length==2){
			JSONObject jObj = new JSONObject();
			try {
				jObj.put("file1",compareFile(map.get(file)[0]));
				jObj.put("file2",compareFile(map.get(file)[1]));
				jObj.put("exist1",new File(map.get(file)[0]).exists());
				jObj.put("exist2",new File(map.get(file)[1]).exists());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jObj.toString();
		}
		return null;
	}
	private String compareFile(String path){
		StringBuffer sb = new StringBuffer();
		File file=new File(path);
		if(file.exists()){
			BufferedReader br=null;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"utf-8"));
				String s=null;
				while((s=br.readLine())!=null)
					sb.append(s+'\n');
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				if(br!=null)
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		return sb.toString();
	}
	private boolean compare(String path1,String path2){
		return compareFile(path1).equals(compareFile(path2));
	}
	private boolean exist(String path1,String path2){
		return new File(path1).exists() && new File(path2).exists();
	}
	private String getMap(ServletContext context,String root){
		BufferedReader br=null;
		StringBuffer sb=new StringBuffer();
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(root),"utf-8"));
			String s=null;
			while((s=br.readLine())!=null)
				sb.append(s+'\n');
		}catch(UnsupportedEncodingException e){e.printStackTrace();
		}catch(IOException e){e.printStackTrace();
		}finally{if(br!=null)try {br.close();} catch (IOException e) {e.printStackTrace();}}
		sb.setLength(sb.length()-1);
		return sb.toString();
	}
	private void writeMap(String map){
		String root=""+mapFile;
		File file = new File(mapFile);if(file.exists())file.setWritable(true);
		BufferedWriter bw=null;
		try {
			bw = new BufferedWriter(new FileWriter(root));
			bw.write(map,0,map.length());
			bw.flush();
		}catch(IOException e){e.printStackTrace();
		}finally{if(bw!=null)try {bw.close();} catch (IOException e) {e.printStackTrace();}}
		if(file.exists())file.setWritable(false);
	}
	private void pasteFile(String file,int m){
		Map<String,String[]> map=mapFile(mapFile);
		String path1=map.get(file)[0];
		String path2=map.get(file)[1];
		if(m==1)pasteFile(path1,path2);
		else if(m==2)pasteFile(path2,path1);
	}
	private void pasteFile(String path1,String path2){
		BufferedInputStream bis=null;
		BufferedOutputStream bos=null;
		try {
			bis = new BufferedInputStream(new FileInputStream(path1));
			bos = new BufferedOutputStream(new FileOutputStream(path2));
			byte[] b=new byte[1024];
			int n=0;
			while((n=bis.read(b))!=-1)
				bos.write(b,0,n);
			bos.flush();
		}catch(IOException e){e.printStackTrace();
		}finally{
			if(bis!=null)try {bis.close();} catch (IOException e) {e.printStackTrace();}
			if(bos!=null)try {bos.close();} catch (IOException e) {e.printStackTrace();}
		}
	}
	private String getPage(String path) {
	BufferedReader br = null;
	CharArrayWriter caw = new CharArrayWriter();
	try {
	    br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(path), "utf8"));
	    char[] c = new char[1024];
	    int n = 0;
	    while ((n = br.read(c)) != -1)
		caw.write(c, 0, n);
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}finally{if(br != null)try{br.close();}catch(IOException e){e.printStackTrace();}}
	return new String(caw.toCharArray());
    }
	private String getFile2f(String file1,String file2){
		if(file1!=null && file2!=null){
			JSONObject jObj = new JSONObject();
			try {
				jObj.put("file1",compareFile(file1));
				jObj.put("file2",compareFile(file2));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jObj.toString();
		}
		return null;
	}
}