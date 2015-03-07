package com.knowledgex.web.view;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knowledgex.domain.Fragment;

@SuppressWarnings("serial")
public final class FragmentBean implements Serializable {
    
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(FragmentBean.class);
    
    private Fragment fragment;
    
    private String concatenatedTagNames;
    
    private boolean checked = false;
    
    public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public String getConcatenatedTagNames() {
		return concatenatedTagNames;
	}

	public void setConcatenatedTagNames(String concatenatedTagNames) {
		this.concatenatedTagNames = concatenatedTagNames;
	}

	public void clear() {
        setConcatenatedTagNames("");
        if (fragment != null) {
            fragment.setId(null);
            fragment.setTitle("");
            fragment.setContent("");
        }
    }
    
    public String toString() {
        return "{tags:" + getConcatenatedTagNames()
                + "}, {" + fragment.toString()
                + "}"
                ;
    }
}
