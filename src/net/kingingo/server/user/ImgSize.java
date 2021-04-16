package net.kingingo.server.user;

import lombok.Getter;

public enum ImgSize {
_256x256(256),
_128x128(128);
	@Getter
	private int size;
	private ImgSize(int size) {
		this.size=size;
	}
	
}
