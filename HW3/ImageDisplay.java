
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;


public class ImageDisplay{

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	BufferedImage imgTwo;
	BufferedImage imgResize;
	int width = 512; // default image width and height
	int height = 512;

	private int[] byteToInt(byte[] bytes){
		int[] data = new int[bytes.length];
		for(int i = 0; i< bytes.length; i++){
			if(bytes[i] < 0){
				data[i] = (int)bytes[i]+256;
			}else{
				data[i] = (int)bytes[i];
			}
		}
		return data;
	}

	private byte[] intToByte(int[] ints){
		byte[] data = new byte[ints.length];
		for(int i =0; i< ints.length; i++){
			if(ints[i]>255){
				ints[i] = 255;
			}else if(ints[i]<0){
				ints[i] = 0;
			}
			if(ints[i]>127){
				data[i] = (byte)(ints[i] - 256);
			}else{
				data[i] = (byte)(ints[i]);
			}
		}
		return data;
	}
	
	private double[] RGBtoYCrCb(int[] bytes){
		double[] YCCbytes = new double[(int)3*width*height];
		for(int i = 0; i < (width*height); i++){
			//Y
			YCCbytes[i] = (
				(double)bytes[i]*(double)(0.299) + 
				(double)bytes[i+width*height]* (double)(0.587)+ 
				(double)bytes[i+2*width*height]*(double)(0.144)
			);
			
			//Cr
			YCCbytes[i+width*height] = (
				128 -
				(double)bytes[i]*(double)(0.168736) - 
				(double)bytes[i+width*height]*(0.331264) + 
				(double)bytes[i+2*width*height]*(double)(0.5)
			);

			//Cb
			YCCbytes[i+2*width*height] = (
				128+
				(double)bytes[i]*(double)(0.5)- 
				(double)bytes[i+width*height]*(double)(0.418688) - 
				(double)bytes[i+2*width*height]*(double)(0.081312)
			); 
		}

		return YCCbytes;
	}

	private int[] YCCtoRGB(double[] bytes){

		int len = bytes.length;
		int[] RGBbytes = new int[len];
		for(int i = 0; i < (len/3); i++){
			//R	
			RGBbytes[i] = (int)Math.round(
				bytes[i] + 
				bytes[i+len/3]*0 + 
				(bytes[i+2*len/3]-128)*(double)(1.402)

			);

			// G
			RGBbytes[i+len/3] = (int)Math.round(
				bytes[i] -
				(bytes[i+len/3]-128)*(double)(0.344136) -
				(bytes[i+2*len/3]-128)*(double)(0.714136) 
			); 

			//B
			RGBbytes[i+2*len/3] = (int)Math.round(
				bytes[i] +
				(bytes[i+len/3]-128)*(double)(1.772) +
				bytes[i+2*len/3]*0
			);
		}

		return RGBbytes;
	}

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private byte[] readImageRGB(int width, int height, String imgPath, BufferedImage img){
		int frameLength = width*height*3;
		long len = frameLength;
		byte[] bytes = new byte[(int) len];
		try
		{
			

			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);


			raf.read(bytes);

			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
					ind++;
				}
			}
			return bytes;
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return bytes;
	}

	private void createAlteredImg(int width, int height, byte[] bytes, BufferedImage img){
			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
					ind++;
				}
			}
	}

	private void displayImg(BufferedImage img){
		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel(new ImageIcon(img));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);
	}

	private double[] encode(double[] data){
		int len = data.length;
		int size = width*height;
		double[] filterOne = new double[len];
		double[] filtered = new double[len];
		//Compress Rows
		int rowCount = 0;
		int incr = 0;
		for(int i = 0; i<size/2; i++){
			if(incr == width/2){
				rowCount++;
				incr =0;
			}
			
			//Y Low Pass
			filterOne[width*rowCount/2+i] = (data[i*2] + data[i*2+1])/2;
			//Y High Pass
			filterOne[width/2+i+width*rowCount/2] = (data[i*2] - data[i*2+1])/2;
			//Cr Low Pass
			filterOne[size+width*rowCount/2+i] = (data[size+i*2] + data[size+i*2+1])/2;
			//Cr HighPass
			filterOne[size+ width/2+i+width*rowCount/2] = (data[size+i*2] - data[size+i*2+1])/2;
			//Cb Low Pass
			filterOne[2*size+width*rowCount/2+i] = (data[2*size+i*2] + data[2*size+i*2+1])/2;
			//Cb High Pass
			filterOne[2*size+ width/2+i+width*rowCount/2] = (data[2*size+i*2] - data[2*size+i*2+1])/2;

			incr++;
		}
		
		//Compress Columns
		int colCount = 0;
		int inc = 0;
		for(int i = 0; i<size/2; i++){
			if(inc == height){
				colCount++;
				inc = 0;
			}	

			//Y Low Pass
			filtered[i] = (filterOne[height*colCount+i] + filterOne[i+(colCount+1)*height])/2;
			//Y High Pass
			filtered[size/2+i] = (filterOne[height*colCount+i] - filterOne[i+(colCount+1)*height])/2;
			//Cr Low Pass
			filtered[size+i] = (filterOne[size+height*colCount+i] + filterOne[size+i+(colCount+1)*height])/2;
			//Cr HighPass
			filtered[3*size/2 + i] = (filterOne[size+height*colCount+i] - filterOne[size+i+(colCount+1)*height])/2;
			//Cb Low Pass
			filtered[2*size+i] = (filterOne[2*size+height*colCount+i] + filterOne[2*size+i+(colCount+1)*height])/2;
			//Cb High Pass
			filtered[5*size/2 +i] = (filterOne[2*size+height*colCount+i] - filterOne[2*size+i+(colCount+1)*height])/2;

			inc++;
		}
		
		return filtered;
	}

	private double[] filter(double[] data, int level){
		int length = width*height*3;
		double[] filtered = new double[length];
		int rowCount = 0;
		int channelCount = 0;
		int incRow = 0;
		int pixelCount = (int)Math.pow(2,level);
		//filter Rows and Columns
		for(int i = 0; i<length; i++){
			if(incRow == width){
				incRow =0;
				rowCount++;
			}
			if(rowCount == height){
				rowCount = 0;
				channelCount++;
			}
			if(i-width*(rowCount+channelCount*width) < pixelCount && rowCount < pixelCount){
				filtered[i] = data[i];
			}else{
				filtered[i] =0;
			}
			incRow++;
		}

		return filtered;
	}

	private double[] decode(double[] data){
		double[] decoded1 = new double[data.length];
		double[] decoded2 = new double[data.length];
		int length = data.length;
		int size = width*height;
		//Decode row
		int rowcount=0;
		int incr = 0;
		for(int i=0; i<length/2;i++){
			if(incr == width/2){
				incr = 0;
				rowcount++;
			}			

			decoded1[2*i] = data[i+rowcount*(width/2)]+data[i+(rowcount+1)*width/2];
			decoded1[2*i+1] = data[i+rowcount*(width/2)] - data[i+(rowcount+1)*width/2];
			incr++;
		}

		//Decode Columns
		int colCount = 0;
		int inc = 0;
		for(int i=0; i<length/6;i++){
			if(inc ==width){
				inc = 0;
				colCount++;
			}
			decoded2[colCount*width+i] = decoded1[i] + decoded1[i+size/2];
			decoded2[(colCount+1)*width+i] =  decoded1[i] - decoded1[i+size/2];

			decoded2[size+colCount*width+i] = decoded1[size+i] + decoded1[size+i+size/2];
			decoded2[size+(colCount+1)*width+i] =  decoded1[size+i] - decoded1[size+i+size/2];

			decoded2[2*size+colCount*width+i] = decoded1[2*size+i] + decoded1[2*size+i+size/2];
			decoded2[2*size+(colCount+1)*width+i] =  decoded1[2*size+i] - decoded1[2*size+i+size/2];

			inc++;
		}
		return decoded2;
	}

	public void showIms(String[] args){

		// Read parameters from command line
		//
		int level = Integer.parseInt(args[1]);

		byte[] Data = new byte[3*width*height];

		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Data = readImageRGB(width, height, args[0], imgOne);
		//displayImg(imgOne);

		//Display modified Img
		imgTwo = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int[] intData = byteToInt(Data);
		double[] YCCData = RGBtoYCrCb(intData);
		for(int i =level; i<9; i++ ){
			if(i==-1){i++;}
			YCCData = encode(YCCData);
		}

		if(level ==-1){
			double[] tempData = new double[YCCData.length];
			for(int i = 0; i<=9; i++){

				tempData = filter(YCCData, i);
				YCCData = decode(YCCData);
				for(int j = i; j<9; j++){	
					tempData = decode(tempData);
				}
				int[] RGBData = YCCtoRGB(tempData);
				byte[] RGBbytes = intToByte(RGBData);
				createAlteredImg(width, height, RGBbytes, imgTwo);
				displayImg(imgTwo);
				
			}
		}else{
			YCCData = filter(YCCData, level);
			for(int i = level; i<9; i++){
				YCCData = decode(YCCData);
			}
			int[] RGBData = YCCtoRGB(YCCData);
			byte[] RGBbytes = intToByte(RGBData);
			createAlteredImg(width, height, RGBbytes, imgTwo);
			displayImg(imgTwo);
		}

	}

	public static void main(String[] args) {
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
	}

}
