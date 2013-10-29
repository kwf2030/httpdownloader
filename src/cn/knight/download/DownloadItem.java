package cn.knight.download;

import java.io.Serializable;

public class DownloadItem implements Serializable {

	private static final long serialVersionUID = -8992445405464944911L;

	public static final int WAITING = -1;
	public static final int STARTED = -2;
	public static final int PAUSED = -3;
	public static final int COMPLETED = -4;
	public static final int FAILED = -5;

	private String proxy;
	private int port;
	private String url;
	private String dir;
	private String name;
	private long block;
	private long size;
	private long pos;
	private int status;
	private int speed;

	public DownloadItem() {
		this(null);
	}

	public DownloadItem(String url) {
		this.proxy = Utils.getConfProperty("proxy");
		this.port = Integer.parseInt(Utils.getConfProperty("port"));
		this.url = url;
		this.dir = Utils.getConfProperty("dir");
		this.name = "download_" + Utils.getTime(0);
		this.block = Long.parseLong(Utils.getConfProperty("block"));
		this.status = WAITING;
		this.speed = -1;
	}

	public String getProxy() {
		return proxy;
	}

	public void setProxy(String proxy) {
		this.proxy = proxy;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getBlock() {
		return block;
	}

	public void setBlock(long block) {
		this.block = block;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getPos() {
		return pos;
	}

	public void setPos(long pos) {
		this.pos = pos;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getSpeed() {
		return speed;
	}
	
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DownloadItem other = (DownloadItem) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("********************FileInfo********************");
		sb.append("\n");
		sb.append("proxy:");
		sb.append(proxy);
		sb.append("\n");
		sb.append("port:");
		sb.append(port);
		sb.append("\n");
		sb.append("url:");
		sb.append(url);
		sb.append("\n");
		sb.append("dir:");
		sb.append(dir);
		sb.append("\n");
		sb.append("name:");
		sb.append(name);
		sb.append("\n");
		sb.append("block:");
		sb.append(block);
		sb.append("\n");
		sb.append("size:");
		sb.append(size);
		sb.append("\n");
		sb.append("pos:");
		sb.append(pos);
		sb.append("\n");
		sb.append("status:");
		sb.append(status);
		sb.append("\n");
		sb.append("speed:");
		sb.append(speed);
		sb.append("\n");
		sb.append("********************FileInfo********************");
		return sb.toString();
	}

}