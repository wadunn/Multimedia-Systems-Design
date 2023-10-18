
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;


public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	BufferedImage imgTwo;
	BufferedImage imgResize;
	int width = 1920; // default image width and height
	int height = 1080;

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
	
	private double[] RGBtoYUV(int[] bytes){
		double[][] RGBtoYUV = { {0.299,0.587,0.114},{0.596,-0.274,-0.322},{0.211,-0.523,0.312} };	
		double[] YUVbytes = new double[(int)3*width*height];
		for(int i = 0; i < (width*height); i++){
			//Y
			YUVbytes[i] = (
				(double)bytes[i]*RGBtoYUV[0][0] + 
				(double)bytes[i+width*height]*RGBtoYUV[0][1] + 
				(double)bytes[i+2*width*height]*RGBtoYUV[0][2]
			);
			
			//U
			YUVbytes[i+width*height] = (
				(double)bytes[i]*RGBtoYUV[1][0] + 
				(double)bytes[i+width*height]*RGBtoYUV[1][1] + 
				(double)bytes[i+2*width*height]*RGBtoYUV[1][2]
			);

			//V
				YUVbytes[i+2*width*height] = (
				(double)bytes[i]*RGBtoYUV[2][0] + 
				(double)bytes[i+width*height]*RGBtoYUV[2][1] + 
				(double)bytes[i+2*width*height]*RGBtoYUV[2][2]
			); 
		}
		/*
			System.out.println("RGB ");
			System.out.println("R: "+bytes[6220255-2*height*width]);
			System.out.println("G: "+bytes[6220255-height*width]);
			System.out.println("B: "+bytes[6220255]);

			System.out.println("YUV ");
			System.out.println("Y: "+YUVbytes[6220255-2*height*width]);
			System.out.println("U: "+YUVbytes[6220255-height*width]);
			//System.out.println("U:1:" + (double)bytes[6220255-2*height*width]*RGBtoYUV[1][0]);
			//System.out.println("U:2:" +(double)bytes[6220255-height*width]*RGBtoYUV[1][1]);
			//System.out.println("U:3:" + (double)bytes[6220255]*RGBtoYUV[1][2]);
			//System.out.println("U sum: "+(double)bytes[6220255-2*height*width]*RGBtoYUV[1][0]+(double)bytes[6220255-height*width]*RGBtoYUV[1][1]+(double)bytes[6220255]*RGBtoYUV[1][2]);
			System.out.println("V: "+YUVbytes[6220255]);
		*/		
		return YUVbytes;
	}

	private int[] YUVtoRGB(double[] bytes){
		double[][] YUVtoRGB = { {1,0.956,0.621},{1,-.272,-.647},{1,-1.106,1.703} };
		int len = bytes.length;
		int[] RGBbytes = new int[len];
		for(int i = 0; i < (len/3); i++){
			//R	
			RGBbytes[i] = (int)Math.round(
				bytes[i]*YUVtoRGB[0][0] + 
				bytes[i+len/3]*YUVtoRGB[0][1] + 
				bytes[i+2*len/3]*YUVtoRGB[0][2]
			);

			// G
			RGBbytes[i+len/3] = (int)Math.round(
				bytes[i]*YUVtoRGB[1][0] + 
				bytes[i+len/3]*YUVtoRGB[1][1] + 
				bytes[i+2*len/3]*YUVtoRGB[1][2]
			); 

			//B
			RGBbytes[i+2*len/3] = (int)Math.round(
				bytes[i]*YUVtoRGB[2][0] + 
				bytes[i+len/3]*YUVtoRGB[2][1] + 
				bytes[i+2*len/3]*YUVtoRGB[2][2]
			);
		}
		//System.out.println("R: "+bytes[6220255-2*len/3]*YUVtoRGB[2][0]);
		//System.out.println("G: "+bytes[6220255-len/3]*YUVtoRGB[2][1]);
		//System.out.println("B: "+bytes[6220255]*YUVtoRGB[2][2]);
		System.out.println("R: "+ RGBbytes[6220255-2*width*height]);
		System.out.println("G: "+ RGBbytes[6220255-width*height]);
		System.out.println("B: "+ RGBbytes[6220255]);
		return RGBbytes;
	}

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private byte[] readImageRGB(int width, int height, String imgPath, BufferedImage img)
	{
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

	private double[] subsample(double[] YUV, int Y, int U, int V){
		int YUVlen = YUV.length;
		int Ylen; int Ulen; int Vlen;

		if(Y==0){Ylen = 0;}
		else{Ylen = (YUVlen/3)/Y;} //new Y sample size
		if(U==0){Ulen = 0;}
		else{Ulen = (YUVlen/3)/U;} //new U sample size
		if(V==0){Vlen = 0;}
		else{Vlen = (YUVlen/3)/V;} //new V sample size

		double[] subsample = new double[Ylen+Ulen+Vlen];
		int ind = 0;
		for(int i = 0; i<(Ylen+Ulen+Vlen); i++){
			subsample[i] = YUV[ind];
			if(i<Ylen){ind+=Y;}
			else if(i<(Ylen+Ulen)){ind+=U;}
			else{ind+=V;}
		}
		System.out.println("original size: "+YUVlen);
		System.out.println("Subsample size: "+subsample.length);
		return subsample;
	}

	private double[] upsample(double[] sub, int Y, int U, int V){
		
		int Ylen; int Ulen; int Vlen;

		if(Y==0){Ylen = 0;}
		else{Ylen = width*height/Y;} //new Y sample size
		if(U==0){Ulen = 0;}
		else{Ulen = width*height/U;} //new U sample size
		if(V==0){Vlen = 0;}
		else{Vlen = width*height/V;} //new V sample size

		int length = Ylen+Ulen+Vlen;
		//System.out.println("Pre sample size: " + length);
		double[] upsample = new double[3*width*height];
		int ind = 0;
		for(int i = 0; i<Ylen; i++){
				
			upsample[ind] = sub[i];	
			ind++;
			while(ind%Y != 0 && i < (Ylen )){	
				if((i+1)%(width/Y) ==0){
					upsample[ind]=sub[i];
				}else{	
					upsample[ind] = (sub[i] + sub[i+1])/2;
				}
				ind++;		
			}
		}
		//System.out.println("Max Y: "+ ind); 
		for(int i = Ylen; i< Ylen+Ulen; i++){
			upsample[ind] = sub[i];
			ind++;	
			while(ind%U !=0 && i <(Ylen+Ulen)){
				if((i+1)%(width/U) ==0){
					upsample[ind]=sub[i];
				}else{	
					upsample[ind] = (sub[i] + sub[i+1])/2;
				}
				ind++;		
			}
		}
		//System.out.println("Max U: "+ ind);
		for(int i = (Ylen+Ulen); i < length; i++){
			upsample[ind] = sub[i];
			ind++;	
			while(ind%V !=0 && i <(sub.length)){
				if((i+1)%(width/V) ==0){
					upsample[ind]=sub[i];
				}else{	
					upsample[ind] = (sub[i] + sub[i+1])/2;
					//upsample[ind] = sub[i];
				}
				ind++;	
			}
		}
		System.out.println("Upsampled Y : "+ upsample[6220255-2*width*height]);	
		System.out.println("Upsampled U : "+ upsample[6220255-width*height]);
		System.out.println("Upsampled V : "+ upsample[6220255]);
		//System.out.println("last byte: "+ upsample[upsample.length-1]);
		return upsample;
	}

	private void difCheck(byte[] d1, byte[] d2){
		for(int i = 1; i <d1.length; i++){
			if(Math.abs(d2[i] - d1[i]) >1 ){
				System.out.println(i+": "+d2[i]+ "/"+d1[i]);
			}
		}
	}

	private void scaleImage(float sw, float sh, int A, BufferedImage oldImg, BufferedImage newImg){
		int x_old;
		int y_old;
		int newWidth = (int)(sw*width);
		int newHeight = (int)(sh* height);
		System.out.println("newwidth: "+ newWidth);
		System.out.println("newHeight: "+ newHeight);
		for(int y = 0; y < newHeight; y++){
			y_old = (int)((float)y/sh);
			for(int x = 0; x< newWidth; x++ ){
				x_old = (int)((float)x/sw);
				if(A == 0){ 
					newImg.setRGB(x,y, oldImg.getRGB(x_old, y_old));
				}else{
					int[] RGB = {0,0,0};
					int pix;
					int count = 0;
					for(int i = -1; i < 2; i++){
						if(x_old + i >= 0 && x_old + i< width){
							for(int j = -1; j < 2; j++){
								if(y_old + j >= 0 && y_old + j< height){
									pix = oldImg.getRGB(x_old + i, y_old + j);
									RGB[0] += (pix>>16)&0xff;
									RGB[1] += (pix>>8)&0xff;
									RGB[2] += (pix>>0)&0xff;
									count++;
								}
							}
						}
					}
					//System.out.println("x: "+ x + "  y: " + y);
					RGB[0] = RGB[0]/count;
					RGB[1] = RGB[1]/count;
					RGB[2] = RGB[2]/count;
					pix = 0xff000000 | ((RGB[0] & 0xff) << 16) | ((RGB[1] & 0xff) << 8) | (RGB[2] & 0xff);
					//System.out.println("pix: "+pix);
					newImg.setRGB(x,y,pix);
				}
			}
		}
	}

	public void showIms(String[] args){

		// Read parameters from command line
		//YUV subsampling parameters
		int Y = Integer.parseInt(args[1]);
		int U = Integer.parseInt(args[2]);
		int V = Integer.parseInt(args[3]);
		//Img Scaling parameters
		float sw = Float.parseFloat(args[4]);
		float sh = Float.parseFloat(args[5]);
		//Antialiasing option 
		int A = Integer.parseInt(args[6]);

		System.out.println("Y:" + Y +" U:" + U + " V:"+ V );
		System.out.println("Sw: " +sw+ " Sh: " +sh+ " A: " +A);

		byte[] Data = new byte[3*width*height];
		//Dislay Original Image
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Data = readImageRGB(width, height, args[0], imgOne);
		displayImg(imgOne);

		//Display modified Img
		imgTwo = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int[] intData = byteToInt(Data);
		double[] YUVData = RGBtoYUV(intData);
		double[] SubData = subsample(YUVData, Y, U, V);
		double[] upData = upsample(SubData, Y, U, V);
		int[] RGBData = YUVtoRGB(upData);
		byte[] RGBbytes = intToByte(RGBData);
		//difCheck(Data, RGBData);
		createAlteredImg(width, height, RGBbytes, imgTwo);
		if(sw <1 || sh < 1){
			imgResize = new BufferedImage((int)(width*sw), (int)(height*sh), BufferedImage.TYPE_INT_RGB);
			scaleImage(sw, sh, A, imgTwo, imgResize);
			displayImg(imgResize);
		}else{
			displayImg(imgTwo);
		}
	}

	public static void main(String[] args) {
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
	}

}
