package org.springframework.social.facebook.api;

public class PagePicture {
	
	private Data data;
	
	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}
	
	public class Data {
		private String url;
		private Boolean is_silhouette;
		private Integer height;
		private Integer width;

		public String getUrl() {
			return url;
		}
		
		public void setUrl(String url) {
			this.url = url;
		}
		
		public Boolean getIs_silhouette() {
			return is_silhouette;
		}
		
		public void setIs_silhouette(Boolean is_silhouette) {
			this.is_silhouette = is_silhouette;
		}
		
		public Integer getHeight() {
			return height;
		}
		
		public void setHeight(Integer height) {
			this.height = height;
		}
		
		public Integer getWidth() {
			return width;
		}
		
		public void setWidth(Integer width) {
			this.width = width;
		}
	}
}