package seamCarve;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

class SeamCarve {
	private Frame f;// 定义窗体
	private MenuBar bar;// 定义菜单栏
	private JLabel label;
	private Menu fileMenu;// 定义"文件"和"子菜单"菜单
	private MenuItem openItem, saveItem, carveItem, closeItem;// 定义条目“退出”和“子条目”菜单项
	private String originPath;
	private String savePath;
	private FileDialog openDia, saveDia;// 定义“打开、保存”对话框

	private BufferedImage img, output;
	private int height, width, nowwidth, nowheight;

	private final static int[][] SOBEL_X = new int[][] { { -1, 0, 1 }, { -1, 0, 1 }, { -1, 0, 1 } };
	private final static int[][] SOBEL_Y = new int[][] { { -1, -1, -1 }, { 0, 0, 0 }, { 1, 1, 1 } };

	SeamCarve() {
		init();
	}

	/* 图形用户界面组件初始化 */
	public void init() {
		f = new Frame("my window");// 创建窗体对象
		f.setBounds(100, 100, 650, 600);// 设置窗体位置和大小

		bar = new MenuBar();// 创建菜单栏
		label = new JLabel();// 创建文本域

		fileMenu = new Menu("文件");// 创建“文件”菜单

		openItem = new MenuItem("打开");// 创建“打开"菜单项
		carveItem = new MenuItem("裁剪");// 创建"裁剪"菜单项
		saveItem = new MenuItem("保存");// 创建“保存"菜单项
		closeItem = new MenuItem("退出");// 创建“退出"菜单项

		fileMenu.add(openItem);// 将“打开”菜单项添加到“文件”菜单上
		fileMenu.add(saveItem);// 将“保存”菜单项添加到“文件”菜单上
		fileMenu.add(closeItem);// 将“退出”菜单项添加到“文件”菜单上
		fileMenu.add(carveItem);

		bar.add(fileMenu);// 将文件添加到菜单栏上

		f.setMenuBar(bar);// 将此窗体的菜单栏设置为指定的菜单栏。

		openDia = new FileDialog(f, "打开", FileDialog.LOAD);
		saveDia = new FileDialog(f, "保存", FileDialog.SAVE);

		f.add(label);// 将文本域添加到窗体内
		myEvent();// 加载事件处理

		f.setVisible(true);// 设置窗体可见

	}

	private void myEvent() {

		// 打开菜单项监听
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				openDia.setVisible(true);// 显示打开文件对话框

				String dirpath = openDia.getDirectory();// 获取打开文件路径并保存到字符串中。
				String fileName = openDia.getFile();// 获取打开文件名称并保存到字符串中

				if (dirpath == null || fileName == null)// 判断路径和文件是否为空
					return;
				else
					label.setIcon(null);// 文件不为空，清空原来文件内容。
				originPath = dirpath + fileName;
				label.setIcon(new ImageIcon(originPath));
				try {
					img = ImageIO.read(new File(originPath)); // 读入文件
					width = img.getWidth(); // 得到源图宽
					height = img.getHeight(); // 得到源图长
					f.setBounds(100, 100, width + 10, height + 30);

				} catch (IOException ee) {
					ee.printStackTrace();
				}
			}

		});

		// 保存菜单项监听
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveDia.setVisible(true);// 显示保存文件对话框
				String dirpath = saveDia.getDirectory();// 获取保存文件路径并保存到字符串中。
				String fileName = saveDia.getFile();//// 获取打保存文件名称并保存到字符串中
				savePath = dirpath + fileName + ".jpeg";

				if (dirpath == null || fileName == null)// 判断路径和文件是否为空
					return;// 空操作
				else {
					try {
						ImageIO.write(output, "JPEG", new File(savePath));// 输出到文件流
					} catch (IOException e1) {
						// 抛出IO异常
						e1.printStackTrace();
					}
				}
			}

		});

		// 裁剪菜单项监听
		carveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				output = new BufferedImage(width/2, height/2, BufferedImage.TYPE_INT_RGB);
				int[] temp = new int[width * height];
				int[][] d = new int[width][height];
				img.getRGB(0, 0, width, height, temp, 0, width);

				int[][] origin = new int[width][height];
				for (int i = 0; i < width; i++) {
					for (int j = 0; j < height; j++) {
						origin[i][j] = temp[i + j * width];
					}
				}

				nowwidth = width;
				nowheight = height;
				produceD(origin, d);

				for (int i = 0; i < width - width / 2; i++) {
					int[][] tempd = new int[nowwidth - 1][nowheight];
					int[][] tempo = new int[nowwidth - 1][nowheight];
					seamXOnce(d, origin, tempd, tempo);
					d = tempd;
					origin = tempo;
				}
				int w = d.length;
				int h = d[0].length;
				int[][] tempc = new int[h][w];
				convert(d,tempc);
				d = tempc;
				w = origin.length;
				h = origin[0].length;
				tempc = new int[h][w];
				convert(origin,tempc);
				origin = tempc;
				swapWH();
				for (int j = 0; j < height - height / 2; j++) {
					int[][] tempd = new int[nowwidth - 1][nowheight];
					int[][] tempo = new int[nowwidth - 1][nowheight];
					seamXOnce(d, origin, tempd, tempo);
					d = tempd;
					origin = tempo;
				}
				w = d.length;
				h = d[0].length;
				tempc = new int[h][w];
				convert(d,tempc);
				d = tempc;
				w = origin.length;
				h = origin[0].length;
				tempc = new int[h][w];
				convert(origin,tempc);
				origin = tempc;
				swapWH();

				temp = new int[nowwidth * nowheight];

				for (int i = 0; i < nowwidth; i++) {
					for (int j = 0; j < nowheight; j++) {
						temp[i + j * nowwidth] = origin[i][j];
					}
				}

				output.setRGB(0, 0, nowwidth, nowheight, temp, 0, nowwidth);
				label.setIcon(new ImageIcon(output));
				f.setBounds(100, 100, nowwidth + 10, nowheight + 30);
			}
		});

		// 退出菜单项监听
		closeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}

		});

		// 窗体关闭监听
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);

			}

		});
	}

	private void seamXOnce(int[][] d, int[][] origin, int[][] tempd, int[][] tempo) {
		int[][] s = new int[nowwidth][nowheight];

		int[][] path = new int[nowwidth][nowheight];
		int[] flag = new int[nowheight];
		for (int i = 0; i < nowwidth; i++) {
			s[i][0] = d[i][0];
			path[i][0] = -1;
		}
		for (int y = 1; y < nowheight; y++) {
			for (int x = 0; x < nowwidth; x++) {
				int temp = s[x][y - 1];
				path[x][y] = x;
				if (x != 0 && s[x][y - 1] > s[x - 1][y - 1]) {
					temp = s[x - 1][y - 1];
					path[x][y] = x - 1;
				}
				if (x != nowwidth - 1 && temp > s[x + 1][y - 1]) {
					temp = s[x + 1][y - 1];
					path[x][y] = x + 1;
				}
				s[x][y] = temp + d[x][y];
			}
		}

		int tempi = 0, min = s[0][nowheight - 1];
		for (int i = 1; i < nowwidth; i++) {
			if (min > s[i][nowheight - 1]) {
				tempi = i;
				min = s[i][nowheight - 1];
			}
		}
		for (int i = nowheight - 1; i >= 0; i--) {
			flag[i] = tempi;
			tempi = path[tempi][i];
		}

		for (int i = 0; i < nowheight; i++) {
			int k = 0;
			for (int j = 0; j < nowwidth - 1; j++) {
				if (flag[i] == j) {
					k++;
				}
				tempo[j][i] = origin[j + k][i];
				tempd[j][i] = d[j + k][i];
			}
		}
		nowwidth--;
	}

	private void swapWH() {
		int temp = nowwidth;
		nowwidth = nowheight;
		nowheight = temp;
	}

	private void convert(int[][] k,int[][] temp) {
		int w = k.length;
		int h = k[0].length;
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				temp[j][i] = k[i][j];
			}
		}
	}

	public static int clamp(int value) {
		return value < 0 ? 0 : (value > 255 ? 255 : value);
	}

	public static void main(String[] args) {
		new SeamCarve();
	}

	private void produceD(int[][] origin, int[][] d) {
		double xred = 0, xgreen = 0, xblue = 0;
		double yred = 0, ygreen = 0, yblue = 0;
		int tr = 0, tg = 0, tb = 0;
		int nx = 0, ny = 0;
		for (int y = 0; y < nowheight; y++) {
			for (int x = 0; x < nowwidth; x++) {
				for (int sx = -1; sx <= 1; sx++) {
					for (int sy = -1; sy <= 1; sy++) {
						nx = sx + x;
						ny = sy + y;
						if (nx < 0 || nx >= nowwidth) {
							nx = x;
						}
						if (ny < 0 || ny >= nowheight) {
							ny = y;
						}
						tr = (origin[nx][ny] >> 16) & 0xff;
						tg = (origin[nx][ny] >> 8) & 0xff;
						tb = origin[nx][ny] & 0xff;

						xred += (SOBEL_X[sx + 1][sy + 1] * tr);
						xgreen += (SOBEL_X[sx + 1][sy + 1] * tg);
						xblue += (SOBEL_X[sx + 1][sy + 1] * tb);

						yred += (SOBEL_Y[sx + 1][sy + 1] * tr);
						ygreen += (SOBEL_Y[sx + 1][sy + 1] * tg);
						yblue += (SOBEL_Y[sx + 1][sy + 1] * tb);
					}
				}

				double mred = Math.sqrt(xred * xred + yred * yred);
				double mgreen = Math.sqrt(xgreen * xgreen + ygreen * ygreen);
				double mblue = Math.sqrt(xblue * xblue + yblue * yblue);
				// (ta << 24) |
				d[x][y] = (clamp((int) mred)*30 + clamp((int) mgreen)*11 + clamp((int) mblue)*59)/100;

				// cleanup for next loop
				xred = xgreen = xblue = 0;
				yred = ygreen = yblue = 0;
			}
		}
	}
}