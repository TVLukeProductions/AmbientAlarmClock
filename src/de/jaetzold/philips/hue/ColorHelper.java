package de.jaetzold.philips.hue;

import java.util.HashMap;
import java.util.Map;

public class ColorHelper {

	private static final Map<String,String> colormap;

	
	public static String convertName2RGB(String colorname) {
		for (String key : colormap.keySet()) {
			if (key.equalsIgnoreCase(colorname)) {
				return colormap.get(key);
			}
		}
		return null;
	}
	
	public static Map<String,Integer> convertRGB2Hue(String rgbcolor) {
		int r = Integer.parseInt(rgbcolor.substring(1,3),16);
		int g = Integer.parseInt(rgbcolor.substring(3,5),16);
		int b = Integer.parseInt(rgbcolor.substring(5,7),16);
		//System.out.println("rgb = "+r+" "+g+" "+b);

		double[] hsv = RGBtoHSV(r,g,b);
		//System.out.println("hsv = "+hsv[0]+" "+hsv[1]+" "+hsv[2]);
		
		Integer hue = (int) Math.round(((hsv[0]/360.0)*65535.0));
		Integer sat = (int) Math.round((hsv[1]/100.0)*255.0);
		Integer bri = (int) Math.round((hsv[2]/100.0)*255.0);

		Map<String,Integer> map = new HashMap<String,Integer>();
		//System.out.println("hsv = "+hue+" "+sat+" "+bri);
		map.put("hue", hue);
		map.put("sat", sat);
		map.put("bri", bri);
		return map;
	}
	
	
	public static double[] RGBtoHSV(double r, double g, double b){

	    double h, s, v;

	    double min, max, delta;

	    min = Math.min(Math.min(r, g), b);
	    max = Math.max(Math.max(r, g), b);

	    // V
	    v = max;

	     delta = max - min;

	    // S
	     if( max != 0 )
	        s = delta / max;
	     else {
	        s = 0;
	        h = -1;
	        return new double[]{h,s,v};
	     }

	    // H
	     if( r == max )
	        h = ( g - b ) / delta; // between yellow & magenta
	     else if( g == max )
	        h = 2 + ( b - r ) / delta; // between cyan & yellow
	     else
	        h = 4 + ( r - g ) / delta; // between magenta & cyan

	     h *= 60;    // degrees

	    if( h < 0 )
	        h += 360;

	    h = h * 1.0;
	    s = s * 100.0;
	    v = (v/256.0)*100.0;
	    return new double[]{h,s,v};
	}
	
	static {
		colormap = new HashMap<String,String>();
		colormap.put("AliceBlue", "#F0F8FF");
		colormap.put("AntiqueWhite", "#FAEBD7");
		colormap.put("Aqua", "#00FFFF");
		colormap.put("Aquamarine", "#7FFFD4");
		colormap.put("Azure", "#F0FFFF");
		colormap.put("Beige", "#F5F5DC");
		colormap.put("Bisque", "#FFE4C4");
		colormap.put("Black", "#000000");
		colormap.put("BlanchedAlmond", "#FFEBCD");
		colormap.put("Blue", "#0000FF");
		colormap.put("BlueViolet", "#8A2BE2");
		colormap.put("Brown", "#A52A2A");
		colormap.put("BurlyWood", "#DEB887");
		colormap.put("CadetBlue", "#5F9EA0");
		colormap.put("Chartreuse", "#7FFF00");
		colormap.put("Chocolate", "#D2691E");
		colormap.put("Coral", "#FF7F50");
		colormap.put("CornflowerBlue", "#6495ED");
		colormap.put("Cornsilk", "#FFF8DC");
		colormap.put("Crimson", "#DC143C");
		colormap.put("Cyan", "#00FFFF");
		colormap.put("DarkBlue", "#00008B");
		colormap.put("DarkCyan", "#008B8B");
		colormap.put("DarkGoldenRod", "#B8860B");
		colormap.put("DarkGray", "#A9A9A9");
		colormap.put("DarkGreen", "#006400");
		colormap.put("DarkKhaki", "#BDB76B");
		colormap.put("DarkMagenta", "#8B008B");
		colormap.put("DarkOliveGreen", "#556B2F");
		colormap.put("DarkOrange", "#FF8C00");
		colormap.put("DarkOrchid", "#9932CC");
		colormap.put("DarkRed", "#8B0000");
		colormap.put("DarkSalmon", "#E9967A");
		colormap.put("DarkSeaGreen", "#8FBC8F");
		colormap.put("DarkSlateBlue", "#483D8B");
		colormap.put("DarkSlateGray", "#2F4F4F");
		colormap.put("DarkTurquoise", "#00CED1");
		colormap.put("DarkViolet", "#9400D3");
		colormap.put("DeepPink", "#FF1493");
		colormap.put("DeepSkyBlue", "#00BFFF");
		colormap.put("DimGray", "#696969");
		colormap.put("DodgerBlue", "#1E90FF");
		colormap.put("FireBrick", "#B22222");
		colormap.put("FloralWhite", "#FFFAF0");
		colormap.put("ForestGreen", "#228B22");
		colormap.put("Fuchsia", "#FF00FF");
		colormap.put("Gainsboro", "#DCDCDC");
		colormap.put("GhostWhite", "#F8F8FF");
		colormap.put("Gold", "#FFD700");
		colormap.put("GoldenRod", "#DAA520");
		colormap.put("Gray", "#808080");
		colormap.put("Green", "#00FF00");
		colormap.put("GreenYellow", "#ADFF2F");
		colormap.put("HoneyDew", "#F0FFF0");
		colormap.put("HotPink", "#FF69B4");
		colormap.put("IndianRed", "#CD5C5C");
		colormap.put("Indigo", "#4B0082");
		colormap.put("Ivory", "#FFFFF0");
		colormap.put("Khaki", "#F0E68C");
		colormap.put("Lavender", "#E6E6FA");
		colormap.put("LavenderBlush", "#FFF0F5");
		colormap.put("LawnGreen", "#7CFC00");
		colormap.put("LemonChiffon", "#FFFACD");
		colormap.put("LightBlue", "#ADD8E6");
		colormap.put("LightCoral", "#F08080");
		colormap.put("LightCyan", "#E0FFFF");
		colormap.put("LightGoldenRodYellow", "#FAFAD2");
		colormap.put("LightGray", "#D3D3D3");
		colormap.put("LightGreen", "#90EE90");
		colormap.put("LightPink", "#FFB6C1");
		colormap.put("LightSalmon", "#FFA07A");
		colormap.put("LightSeaGreen", "#20B2AA");
		colormap.put("LightSkyBlue", "#87CEFA");
		colormap.put("LightSlateGray", "#778899");
		colormap.put("LightSteelBlue", "#B0C4DE");
		colormap.put("LightYellow", "#FFFFE0");
		colormap.put("Lime", "#00FF00");
		colormap.put("LimeGreen", "#32CD32");
		colormap.put("Linen", "#FAF0E6");
		colormap.put("Magenta", "#FF00FF");
		colormap.put("Maroon", "#800000");
		colormap.put("MediumAquaMarine", "#66CDAA");
		colormap.put("MediumBlue", "#0000CD");
		colormap.put("MediumOrchid", "#BA55D3");
		colormap.put("MediumPurple", "#9370DB");
		colormap.put("MediumSeaGreen", "#3CB371");
		colormap.put("MediumSlateBlue", "#7B68EE");
		colormap.put("MediumSpringGreen", "#00FA9A");
		colormap.put("MediumTurquoise", "#48D1CC");
		colormap.put("MediumVioletRed", "#C71585");
		colormap.put("MidnightBlue", "#191970");
		colormap.put("MintCream", "#F5FFFA");
		colormap.put("MistyRose", "#FFE4E1");
		colormap.put("Moccasin", "#FFE4B5");
		colormap.put("NavajoWhite", "#FFDEAD");
		colormap.put("Navy", "#000080");
		colormap.put("OldLace", "#FDF5E6");
		colormap.put("Olive", "#808000");
		colormap.put("OliveDrab", "#6B8E23");
		colormap.put("Orange", "#FFA500");
		colormap.put("OrangeRed", "#FF4500");
		colormap.put("Orchid", "#DA70D6");
		colormap.put("PaleGoldenRod", "#EEE8AA");
		colormap.put("PaleGreen", "#98FB98");
		colormap.put("PaleTurquoise", "#AFEEEE");
		colormap.put("PaleVioletRed", "#DB7093");
		colormap.put("PapayaWhip", "#FFEFD5");
		colormap.put("PeachPuff", "#FFDAB9");
		colormap.put("Peru", "#CD853F");
		colormap.put("Pink", "#FFC0CB");
		colormap.put("Plum", "#DDA0DD");
		colormap.put("PowderBlue", "#B0E0E6");
		colormap.put("Purple", "#800080");
		colormap.put("Red", "#FF0000");
		colormap.put("RosyBrown", "#BC8F8F");
		colormap.put("RoyalBlue", "#4169E1");
		colormap.put("SaddleBrown", "#8B4513");
		colormap.put("Salmon", "#FA8072");
		colormap.put("SandyBrown", "#F4A460");
		colormap.put("SeaGreen", "#2E8B57");
		colormap.put("SeaShell", "#FFF5EE");
		colormap.put("Sienna", "#A0522D");
		colormap.put("Silver", "#C0C0C0");
		colormap.put("SkyBlue", "#87CEEB");
		colormap.put("SlateBlue", "#6A5ACD");
		colormap.put("SlateGray", "#708090");
		colormap.put("Snow", "#FFFAFA");
		colormap.put("SpringGreen", "#00FF7F");
		colormap.put("SteelBlue", "#4682B4");
		colormap.put("Tan", "#D2B48C");
		colormap.put("Teal", "#008080");
		colormap.put("Thistle", "#D8BFD8");
		colormap.put("Tomato", "#FF6347");
		colormap.put("Turquoise", "#40E0D0");
		colormap.put("Violet", "#EE82EE");
		colormap.put("Wheat", "#F5DEB3");
		colormap.put("White", "#FFFFFF");
		colormap.put("WhiteSmoke", "#F5F5F5");
		colormap.put("Yellow", "#FFFF00");
		colormap.put("YellowGreen", "#9ACD32");
		
		
		
	}
	
	
	

}
