package brevis.graphics;

import javax.vecmath.Vector3d;

/*public class BrMesh {
	public float[] verts;
	public float[] col;
	public int[] idx;
	
	public int numVerts() {
		return verts.length;
	}
	
	public int numCol() {
		return col.length;
	}
	
	public int numIdx() {
		return idx.length;
	}


}*/



import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

/**
 * derived from code by:
 * @author Jeremy Adams (elias4444)
 *
 * Use these lines if reading from a file
 * FileReader fr = new FileReader(ref);
 * BufferedReader br = new BufferedReader(fr);

 * Use these lines if reading from within a jar
 * InputStreamReader fr = new InputStreamReader(new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(ref)));
 * BufferedReader br = new BufferedReader(fr);
 */

public class BrMesh {
	
	
	public ArrayList<float[]> vertexsets = new ArrayList<float[]>(); // Vertex Coordinates
	public ArrayList<float[]> vertexsetsnorms = new ArrayList<float[]>(); // Vertex Coordinates Normals
	public ArrayList<float[]> vertexsetstexs = new ArrayList<float[]>(); // Vertex Coordinates Textures
	public ArrayList<int[]> faces = new ArrayList<int[]>(); // Array of Faces (vertex sets)
	public ArrayList<int[]> facestexs = new ArrayList<int[]>(); // Array of of Faces textures
	public ArrayList<int[]> facesnorms = new ArrayList<int[]>(); // Array of Faces normals
	
	private int objectlist;
	private int numpolys = 0;
	
	//// Statisitcs for drawing ////
	public float toppoint = 0;		// y+
	public float bottompoint = 0;	// y-
	public float leftpoint = 0;		// x-
	public float rightpoint = 0;	// x+
	public float farpoint = 0;		// z-
	public float nearpoint = 0;		// z+
	
	public String toString() {
		String s = "#BrMesh{ :numpolys " + numpolys + ", :toppoint " + toppoint +
				", :bottompoint " + bottompoint + ", :leftpoint " + leftpoint +
				", :rightpoint " + rightpoint + ", :farpoint " + farpoint +
				", :nearpoint " + nearpoint + 
				"}";		 				 				
		return s;
	}
	
	public BrMesh(BufferedReader ref, boolean centerit) {
		loadobject(ref);
		if (centerit) {
			centerit();
		}
		opengldrawtolist();
		numpolys = faces.size();
		// We don't actually want to cleanup
		//cleanup();
	}
	
	@SuppressWarnings("unused")
	private void cleanup() {
		vertexsets.clear();
		vertexsetsnorms.clear();
		vertexsetstexs.clear();
		faces.clear();
		facestexs.clear();
		facesnorms.clear();
	}
	
	private void loadobject(BufferedReader br) {
		int linecounter = 0;
		try {
			
			String newline;
			boolean firstpass = true;
			
			while (((newline = br.readLine()) != null)) {
				linecounter++;
				newline = newline.trim();
				if (newline.length() > 0) {
					if (newline.charAt(0) == 'v' && newline.charAt(1) == ' ') {
						float[] coords = new float[4];
						String[] coordstext = new String[4];
						coordstext = newline.split("\\s+");
						for (int i = 1;i < coordstext.length;i++) {
							coords[i-1] = Float.valueOf(coordstext[i]).floatValue();
						}
						//// check for farpoints ////
						if (firstpass) {
							rightpoint = coords[0];
							leftpoint = coords[0];
							toppoint = coords[1];
							bottompoint = coords[1];
							nearpoint = coords[2];
							farpoint = coords[2];
							firstpass = false;
						}
						if (coords[0] > rightpoint) {
							rightpoint = coords[0];
						}
						if (coords[0] < leftpoint) {
							leftpoint = coords[0];
						}
						if (coords[1] > toppoint) {
							toppoint = coords[1];
						}
						if (coords[1] < bottompoint) {
							bottompoint = coords[1];
						}
						if (coords[2] > nearpoint) {
							nearpoint = coords[2];
						}
						if (coords[2] < farpoint) {
							farpoint = coords[2];
						}
						/////////////////////////////
						vertexsets.add(coords);
					}
					if (newline.charAt(0) == 'v' && newline.charAt(1) == 't') {
						float[] coords = new float[4];
						String[] coordstext = new String[4];
						coordstext = newline.split("\\s+");
						for (int i = 1;i < coordstext.length;i++) {
							coords[i-1] = Float.valueOf(coordstext[i]).floatValue();
						}
						vertexsetstexs.add(coords);
					}
					if (newline.charAt(0) == 'v' && newline.charAt(1) == 'n') {
						float[] coords = new float[4];
						String[] coordstext = new String[4];
						coordstext = newline.split("\\s+");
						for (int i = 1;i < coordstext.length;i++) {
							coords[i-1] = Float.valueOf(coordstext[i]).floatValue();
						}
						vertexsetsnorms.add(coords);
					}
					if (newline.charAt(0) == 'f' && newline.charAt(1) == ' ') {
						String[] coordstext = newline.split("\\s+");
						int[] v = new int[coordstext.length - 1];
						int[] vt = new int[coordstext.length - 1];
						int[] vn = new int[coordstext.length - 1];
						
						for (int i = 1;i < coordstext.length;i++) {
							String fixstring = coordstext[i].replaceAll("//","/0/");
							String[] tempstring = fixstring.split("/");
							v[i-1] = Integer.valueOf(tempstring[0]).intValue();
							if (tempstring.length > 1) {
								vt[i-1] = Integer.valueOf(tempstring[1]).intValue();
							} else {
								vt[i-1] = 0;
							}
							if (tempstring.length > 2) {
								vn[i-1] = Integer.valueOf(tempstring[2]).intValue();
							} else {
								vn[i-1] = 0;
							}
						}
						faces.add(v);
						facestexs.add(vt);
						facesnorms.add(vn);
					}
				}
			}
			
		} catch (IOException e) {
			System.out.println("Failed to read file: " + br.toString());
			//System.exit(0);			
		} catch (NumberFormatException e) {
			System.out.println("Malformed OBJ (on line " + linecounter + "): " + br.toString() + "\r \r" + e.getMessage());
			//System.exit(0);
		}
		
	}
	
	private void centerit() {
		float xshift = (rightpoint-leftpoint) /2f;
		float yshift = (toppoint - bottompoint) /2f;
		float zshift = (nearpoint - farpoint) /2f;
		
		for (int i=0; i < vertexsets.size(); i++) {
			float[] coords = new float[4];
			
			coords[0] = ((float[])(vertexsets.get(i)))[0] - leftpoint - xshift;
			coords[1] = ((float[])(vertexsets.get(i)))[1] - bottompoint - yshift;
			coords[2] = ((float[])(vertexsets.get(i)))[2] - farpoint - zshift;
			
			vertexsets.set(i,coords); // = coords;
		}
		
	}
	
	public float getXWidth() {
		float returnval = 0;
		returnval = rightpoint - leftpoint;
		return returnval;
	}
	
	public float getYHeight() {
		float returnval = 0;
		returnval = toppoint - bottompoint;
		return returnval;
	}
	
	public float getZDepth() {
		float returnval = 0;
		returnval = nearpoint - farpoint;
		return returnval;
	}
	
	public int numpolygons() {
		return numpolys;
	}
	
	public void opengldrawtolist() {
		
		this.objectlist = GL11.glGenLists(1);
		
		GL11.glNewList(objectlist,GL11.GL_COMPILE);
		for (int i=0;i<faces.size();i++) {
			int[] tempfaces = (int[])(faces.get(i));
			int[] tempfacesnorms = (int[])(facesnorms.get(i));
			int[] tempfacestexs = (int[])(facestexs.get(i));
			
			//// Quad Begin Header ////
			int polytype;
			if (tempfaces.length == 3) {
				polytype = GL11.GL_TRIANGLES;
			} else if (tempfaces.length == 4) {
				polytype = GL11.GL_QUADS;
			} else {
				polytype = GL11.GL_POLYGON;
			}
			GL11.glBegin(polytype);
			////////////////////////////
			
			for (int w=0;w<tempfaces.length;w++) {
				if (tempfacesnorms[w] != 0) {
					float normtempx = ((float[])vertexsetsnorms.get(tempfacesnorms[w] - 1))[0];
					float normtempy = ((float[])vertexsetsnorms.get(tempfacesnorms[w] - 1))[1];
					float normtempz = ((float[])vertexsetsnorms.get(tempfacesnorms[w] - 1))[2];
					GL11.glNormal3f(normtempx, normtempy, normtempz);
				}
				
				if (tempfacestexs[w] != 0) {
					float textempx = ((float[])vertexsetstexs.get(tempfacestexs[w] - 1))[0];
					float textempy = ((float[])vertexsetstexs.get(tempfacestexs[w] - 1))[1];
					float textempz = ((float[])vertexsetstexs.get(tempfacestexs[w] - 1))[2];
					//GL11.glTexCoord3f(textempx,1f-textempy,textempz);
					//System.out.println( "tx: " + textempx + " " + ( 1f-textempy ) + " " + textempz);
					GL11.glTexCoord2f(textempx,1f-textempy);
				}
				
				float tempx = ((float[])vertexsets.get(tempfaces[w] - 1))[0];
				float tempy = ((float[])vertexsets.get(tempfaces[w] - 1))[1];
				float tempz = ((float[])vertexsets.get(tempfaces[w] - 1))[2];
				GL11.glVertex3f(tempx,tempy,tempz);
				//System.out.println( "v: " + tempx + " " + tempy + " " + tempz );
			}
			
			
			//// Quad End Footer /////
			GL11.glEnd();
			///////////////////////////
			
			
		}
		GL11.glEndList();
	}
	
	public void opengldraw() {
		GL11.glCallList(objectlist);
	}
	
	public float[] trimeshVertices( ) {
		return trimeshVertices( new float[]{ 1.0f, 1.0f,1.0f } );
	}
	
	// These just go ahead and assume data is setup as triangles. 
	public float[] trimeshVertices( float[] scale ) {
		float[] Vertices = new float[vertexsets.size()*3];
		
		for( int k = 0; k < vertexsets.size(); k++ ) {
			final float[] v = vertexsets.get(k);
			Vertices[ k * 3 + 0 ] = scale[0] * v[0];
			Vertices[ k * 3 + 1 ] = scale[1] * v[1];
			Vertices[ k * 3 + 2 ] = scale[2] * v[2];
		}
		//System.out.println( "trimeshVertices " + Vertices.length );
		return Vertices;
	}
	
	public int[] trimeshIndices() {		
	    int[] Indices = new int[faces.size()*3];
	    
	    for( int k = 0; k < faces.size(); k++ ) {
			final int[] f = faces.get(k);
			// index system is rooted at 1
			Indices[ k * 3 + 0 ] = f[0] - 1;
			Indices[ k * 3 + 1 ] = f[1] - 1;
			Indices[ k * 3 + 2 ] = f[2] - 1;
		}
		
	    //System.out.println( "trimeshIndices " + Indices.length );
		return Indices;
	}
	
	public void rescaleMesh( float w, float h, float d ) {		
		
		for( int k = 0; k < vertexsets.size(); k++ ) {
			float[] v = vertexsets.get(k);
			v[ 0 ] = w * v[0];
			v[ 1 ] = h * v[1];
			v[ 2 ] = d * v[2];
			vertexsets.set(k, v);
		}
		//System.out.println( "trimeshVertices " + Vertices.length );
		
		toppoint *= h;		// y+
		bottompoint *= h;	// y-
		leftpoint *= w;		// x-
		rightpoint *= w;	// x+
		farpoint *= d;		// z-
		nearpoint *= d;		// z+
		
		opengldrawtolist();
		// Regen display list
	}
}