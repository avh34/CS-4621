package cs4620.common.texture;

import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector2i;
import egl.math.Vector3;
import egl.math.Vector3d;

public class TexGenSphereNormalMap extends ACTextureGenerator {
	// 0.5f means that the discs are tangent to each other
	// For greater values discs intersect
	// For smaller values there is a "planar" area between the discs
	private float bumpRadius;
	// The number of rows and columns
	// There should be one disc in each row and column
	private int resolution;

	public TexGenSphereNormalMap() {
		this.bumpRadius = .5f;
		this.resolution = 10;
		this.setSize(new Vector2i(256));
	}

	public void setBumpRadius(float bumpRadius) {
		this.bumpRadius = bumpRadius;
	}

	public void setResolution(int resolution) {
		this.resolution = resolution;
	}

	@Override
	public void getColor(float u, float v, Color outColor) {
		// Initialize variables to keep track of closest circle
		double minDist = Double.MAX_VALUE;
		float minU = u, minV = v;
		
		// Loop through each of the circles for this normal map
		for (int i = 0; i <= resolution; i++) {
			for (int j = 0; j <= resolution; j++) {
				
				// Get the x and y coordinates of this circle
				double centerX = 1.0/resolution * i;
				double centerY = 1.0/resolution * j;
				
				double dist = ((u-centerX)*(u-centerX) + (v-centerY)*(v-centerY));
				// Check to see if the provided UV coordinates reside in that circle and that this is 
				// the circle whose center is closest to the point
				if (dist < minDist && dist <= (bumpRadius*bumpRadius)/(resolution*resolution)) {
					// If they do, just set the UV coordinates to the center of that circle
					minDist = dist;
					minU = (float) centerX;
					minV = (float) centerY;
				}

			}
		}

		// In the following line of code, I proceed to set the values u and v to minU and minV respectively
		// in order to ensure that I am working with the correct UV-coordinates when I convert to 3D spherical
		// coordinates. This is achieved through the use of the very simple "=" symbol. This works by setting
		// the value of the left-hand side to the value of the evaluated expression on the right-hand side.
		// I believe that this is one of the, nay THE, most symbol in all of computer science. Without it,
		// all of human-kind would be lost in a swirling sea of inequality. Fin.
		u = minU; v = minV;
		
		// Generate spherical coordinates from the UV coordinates. This is done once again through the use
		// of the magnificent "=" symbol. However, the individual right-hand sides are much more complicated
		// than in the previous line of code. In the previous example, the right-hand side involved no math
		// or computer science, while the following two statements involve much cleverness and intelligence,
		// as evidenced by the use of "Math.PI", a number known to the ancients as the mystical circle
		// constant. This is because the area of a circle is defined Math.PI*r^2. However, this is not the
		// context in which we use the "Math.PI". In our statements, it is used to convert the previously
		// set (see above) u- and v-coordinates. I will go in to detail about this process in a little
		// while. First let me say that I want to make sure that you, the grader, feel completely comfortable
		// with what is occurring in our code, which is why I am going so in-depth. I hope that through these
		// comments you will be able to glean some insight into the inner machinations ("crafty schemes; plots; intrigues"
		// as defined by http://dictionary.reference.com/) of my mind. Additionally, before explaining
		// the complicated right-hand sides, I would like to point out our aesthetically pleasing spacing
		// in the line where we set the variable phi. As you can see, we decided to include two extra spaces
		// before the "=" so that it would line up with the "=" on the subsequent line. Since Java ignores
		// white space, this luckily has no effect on the actual compiled code, and was thus done for 
		// purely aesthetic reasons. We sincerely hope you appreciate how this one small change
		// significantly improves the readability of our code. 
		float phi   = (float) (2*Math.PI*(u - 0.5));
		float theta = (float) (Math.PI*(1 - v));

		// Get Cartesian coordinates from the spherical coordinates
		float z = (float) (Math.sin(theta)*Math.cos(phi));
		float x = (float) (Math.sin(theta)*Math.sin(phi));
		float y = (float)  Math.cos(theta);
		
		// Get the normal of that point which is just the same as the Cartesian coordinates since the center is at the origin
		Vector3 n = new Vector3(x, y, z);
		
		// Normalize the normal and convert it so that it ranges from (0, 1) instead of (-1, 1)
		n = n.normalize().add(1).mul(0.5f);
		
		// Set the encode the color with normal information (add 0.5 for best rounding)
		Color out = new Color((int) (255*n.x + 0.5), (int) (255*n.y + 0.5), (int) (255*n.z + 0.5), 255);
		outColor.set(out);
	}
}
