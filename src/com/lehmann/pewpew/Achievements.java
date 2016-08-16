package com.lehmann.pewpew;

public enum Achievements {

	FIRST_EXTRA_BALL("CgkI543085cFEAIQAQ"),//
	DOUBLE_POINTS("CgkI543085cFEAIQAg"),//
	TRIPLE_POINTS("CgkI543085cFEAIQAw"),//
	FOURTY_POINTS("CgkI543085cFEAIQBA"),//
	MAMBO_NUMBER_FIVE("CgkI543085cFEAIQBQ");//
	
	private String id;

	private Achievements(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

}
