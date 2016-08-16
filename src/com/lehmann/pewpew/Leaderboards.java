package com.lehmann.pewpew;

public enum Leaderboards {

	RANKING("CgkI543085cFEAIQAA");
	
	private String id;

	private Leaderboards(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

}
