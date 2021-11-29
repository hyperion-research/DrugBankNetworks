package drugbank;
/**
 * <copyright>
 * 
 * Copyright (c) Alexandru Topirceanu. alex.topirceanu@yahoo.com All rights
 * reserved.
 * 
 * File created by Alexandru Jul 20, 2015 7:30:34 PM </copyright>
 */

import java.util.HashMap;
import java.util.Map;

public class Drug {
	private String id, name;
	private Map<String, String> interactions;
	private Map<String, String> targets;
	private Map<String, TargetAction> targetActions;
	// used for filtering out drugs
	private boolean isExperimental, isTopical, isATC;
	private String ATCCode = "";
	private int age;
	private TargetAction action;

	/**
	 * Possible target actions
	 */
	public enum TargetAction {
		INHIBITOR, ACTIVATOR, ANTAGONIST, AGONIST, NONE, UNKNOWN, OTHER
	}

	public Drug(String id, String name) {
		this.id = id;
		this.name = name;
		this.isExperimental = true;
		this.isTopical = false;
		this.isATC = false;
		this.age = -1;
		interactions = new HashMap<String, String>();
		targets = new HashMap<String, String>();
		targetActions = new HashMap<String, TargetAction>();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getATCCode() {
		return ATCCode;
	}

	public int getAge() {
		return age;
	}

	public TargetAction getTargetAction() {
		return action;
	}

	public void addInteraction(String id, String name) {
		if (!interactions.keySet().contains(id)) {
			interactions.put(id, name);
		}
	}

	public void addTarget(String id, String name, TargetAction action) {
		if (!targets.keySet().contains(id)) {
			targets.put(id, name);
			targetActions.put(id, action);
		}
	}

	public void setExperimental(boolean isExperimental) {
		this.isExperimental &= isExperimental;
	}

	public void setTopical(boolean isTopical) {
		this.isTopical |= isTopical;
	}

	public void setATC(boolean isATC) {
		this.isATC |= isATC;
	}

	public void setATCCode(String ATCCode) {
		this.ATCCode = ATCCode;
	}

	public void mergeATCCode(String ATCCode) {
		if (!this.ATCCode.contains(ATCCode))
			this.ATCCode += "," + ATCCode;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Map<String, String> getInteractionsMap() {
		return interactions;
	}

	public Map<String, String> getTargetsMap() {
		return targets;
	}

	public Map<String, TargetAction> getTargetActionsMap() {
		return targetActions;
	}

	public boolean isExperimental() {
		return isExperimental;
	}

	public boolean isTopical() {
		return isTopical;
	}

	public boolean isATC() {
		return isATC;
	}

	public void setTargetAction(TargetAction action) {
		this.action = action;
	}

	@Override
	public String toString() {
		return name + " [" + id + "] has " + interactions.size() + " interactions and " + targets.size() + " targets.";
	}

}
