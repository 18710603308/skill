package com.cjl.skill.exception;

public class SkillException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public SkillException() {
		super("skill exception");
	}
	
	public SkillException(String message) {
		super(message);
	}
}