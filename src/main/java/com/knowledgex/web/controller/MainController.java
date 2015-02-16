package com.knowledgex.web.controller;

import java.util.*;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.knowledgex.dao.FragmentDao;
import com.knowledgex.dao.TagDao;
import com.knowledgex.domain.Fragment;
import com.knowledgex.domain.FragmentOrder;
import com.knowledgex.domain.Tag;
import com.knowledgex.web.view.*;

@Controller
@Component("mainController")
public final class MainController {
	
	private static final int MAX_FRAGMENT_PANELS = 3;
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
	@Autowired
	private FragmentDao fragmentDao;

	@Autowired
	private TagDao tagDao;
	
	private Tag trashTag = null;
	
	private Tag getTrashTag() {
		if (trashTag != null) {
			return trashTag;
		}
		trashTag = tagDao.findById(Tag.TRASH_TAG_ID);
		return trashTag;
	}
	
	public List<FragmentListBean> newFragmentListBeans() {
		final List<FragmentListBean> output =  new ArrayList<FragmentListBean>(MAX_FRAGMENT_PANELS);
		for (int i=0; i<MAX_FRAGMENT_PANELS; ++i) {
			final FragmentListBean flb = new FragmentListBean();
			final long tagId = (i == 0) ?
					PanelContextBean.ALL_VALID_TAGS : PanelContextBean.EMPTY_TAG;
			flb.setPanelContextBean(new PanelContextBean(i, tagId));
			output.add(flb);
		}
		return output;
	}
	
	public void populateFragmentListBeans(List<FragmentListBean> flbs, PanelContextBean pcb) {
		final PanelContextBean[] pcbs = new PanelContextBean[MAX_FRAGMENT_PANELS];
		if (pcb != null) {
			pcbs[pcb.getPanelId()] = pcb;
		}
		for (int i=0; i<MAX_FRAGMENT_PANELS; ++i) {
			populateFragmentListBean(flbs.get(i), pcbs[i]);
		}
	}

	private FragmentListBean populateFragmentListBean(FragmentListBean existingFlb, PanelContextBean pcb) {
        final FragmentListBean flb = existingFlb;
        
        if (pcb == null) {
        	pcb = flb.getPanelContextBean();
        }
        
        final long tagId = pcb.getTagId();
        final int curPage = pcb.isLastPage() ? (pcb.getCurPage() - 1) : Math.max(0, pcb.getCurPage());
        final int count = pcb.getItemsPerPage();
        final int first = curPage * count;
        final FragmentOrder frgOrder = FragmentOrder.values()[flb.getOrderOption()];
        final boolean asc = flb.isOrderAsc();
        
        List<Fragment> fragments = null;
        if (tagId == PanelContextBean.ALL_VALID_TAGS) {
        	// Fetch the fragments regardless of tags
        	fragments = fragmentDao.findSomeNonTrashed(first, count + 1, frgOrder, asc);
        }
        else if (tagId == PanelContextBean.EMPTY_TAG) {
        	fragments = Collections.emptyList();
        }
        else if (tagId == Tag.TRASH_TAG_ID) {
        	// Fetch the trashed fragments
            fragments = fragmentDao.findSomeByTagId(tagId, first, count + 1, frgOrder, asc);
        }
        else {
        	// Fetch the fragments with the specified tag (non-trashed)
            fragments = fragmentDao.findSomeNonTrashedByTagId(tagId, first, count + 1, frgOrder, asc);
        }
        
        final boolean isLastPage = fragments.size() <= count;
        final boolean givenTagIsTrashTag = Tag.isTrashTag(tagId);
        flb.setPanelContextBean(new PanelContextBean(pcb.getPanelId(), tagId, curPage, count, isLastPage, givenTagIsTrashTag));
//        ViewUtil.addMessage("pcb", flb.getPanelContextBean());
        
        // [NOTE] The content of fragments should be IMMUTABLE form here!
        
        final List<FragmentBean> fragmentBeans = new ArrayList<FragmentBean>();
        final int c = Math.min(count, fragments.size());
       	for (int i=0; i<c; ++i) {
       		Fragment f = fragments.get(i);
        	FragmentBean fb = new FragmentBean();
        	fb.setFragment(f);
        	final String tagNames = Tag.getTagNamesFrom(f.getTags());
        	fb.setConcatenatedTagNames(tagNames);
        	fragmentBeans.add(fb);
        }
        flb.setFragmentBeans(fragmentBeans);
        
        return flb;
    }

	public FragmentBean newFragmentBean() {
	    final FragmentBean fragmentBean = new FragmentBean();
	    final Fragment frg = new Fragment();
	    fragmentBean.setFragment(frg);
	    return fragmentBean;
	}
	
	public TagListBean newTagListBean() {
		final TagListBean tagListBean = new TagListBean();
		final List<Tag> tags = tagDao.findAllWithChildren();
	    tagListBean.setTags(tags);
	    final int tc = tags.size();
	    List<Long> fcList = new ArrayList<Long>(tc);
	    final boolean includeTrashed = false;
	    for (int i=0; i<tc; ++i) {
	        fcList.add(fragmentDao.countByTagId(tags.get(i).getId(), includeTrashed));
	    }
	    
	    // special handling for the trash tag; count everything
	    final int indexOfTrash = tagListBean.indexOf(Tag.TRASH_TAG_ID);
	    fcList.set(indexOfTrash, fragmentDao.countByTagId(Tag.TRASH_TAG_ID, true));
	    
	    tagListBean.setFragmentCountList(fcList);
	    final TagTree tagTree = newTagTree();
	    tagListBean.setTagTree(tagTree);
        return tagListBean;
    }
	
	public TagBean newTagBean() {
		final TagBean tagBean = new TagBean();
		final Tag tag = new Tag();
		tagBean.setTag(tag);
		return tagBean;
	}
	
	private TagTree newTagTree() {
		final TagTree tagTree = new TagTree();
	    return tagTree;
	}

	public PanelContextBean newPanelContextBean(int panelId, long tagId, int curPage) {
		return new PanelContextBean(panelId, tagId, curPage);
	}
	
	public void trashFragment(Long fragmentId) {
		final Fragment frg = fragmentDao.findById(fragmentId, true, false);
		frg.addTag(getTrashTag());
		fragmentDao.save(frg);
		ViewUtil.addMessage("Trashing", "Fragment #" + frg.getId(), null);
	}

	public void trashFragment(FragmentBean fb) {
		trashFragment(fb.getFragment().getId());
	}

	private void trashFragments(List<Long> fragmentIds) {
		for (Long id : fragmentIds) {
			trashFragment(id);
		}
	}

	public void trashFragments(FragmentListBean flb) {
		final Collection<FragmentBean> fragmentBeans = flb.getFragmentBeans();
		for (FragmentBean fb : fragmentBeans) {
			if (!fb.isChecked()) {
				continue;
			}
			trashFragment(fb.getFragment().getId());
		}
	}

	public void deleteFragment(Long fragmentId) {
		final Fragment frg = fragmentDao.findById(fragmentId);
		fragmentDao.delete(frg);
		ViewUtil.addMessage("Deleting", "Fragment #" + frg.getId(), null);
	}

	public void deleteFragment(FragmentBean fb) {
		deleteFragment(fb.getFragment().getId());
	}
	
	public void deleteFragments(FragmentListBean flb) {
		final Collection<FragmentBean> fragmentBeans = flb.getFragmentBeans();
		for (FragmentBean fb : fragmentBeans) {
			if (!fb.isChecked()) {
				continue;
			}
			deleteFragment(fb.getFragment().getId());
		}
	}
	
	public void saveFragment(FragmentBean fb, TagListBean tagListBean) {
		final String tagNames = fb.getConcatenatedTagNames();
		final Set<Tag> tags = saveTagsWhenSavingFragment(tagListBean, tagNames);
	    
	    Fragment frg = fb.getFragment();
	    boolean weHaveNewFragment = false;
	    
	    final DateTime dt = new DateTime();
	    if (frg.getId() == null) {
	    	// It is a new fragment...
	    	frg.setCreationDatetime(dt);
	    	weHaveNewFragment = true;
	    }
	    else {
	    	// It is an existing fragment...
	    	final String content = frg.getContent();
	    	frg = fragmentDao.findById(frg.getId());
	    	frg.setContent(content);
	    }
	    frg.setUpdateDatetime(dt);

	    frg.setTags(tags);
	    
        fragmentDao.save(frg);
        if (weHaveNewFragment) {
        	ViewUtil.addMessage("Creating", "Fragment #" + frg.getId(), null);
        }
        else {
		    ViewUtil.addMessage("Updating", "Fragment #" + frg.getId(), null);
		}
	}
	
	private void saveTag(Tag t) {
		final DateTime dt = new DateTime();
	    if (t.getId() == null) {
	    	// It is a new tag...
	    	t.setCreationDatetime(dt);
	    }
	    t.setUpdateDatetime(dt);
	    
		tagDao.save(t);
	}
	
	private Set<Tag> saveTagsWhenSavingFragment(TagListBean tagListBean, String tagNames) {
		// [NOTE] this method should be called only when fragments are saved as its name implies
		final Collection<Tag> existingTags = tagListBean.getTags();
		final Collection<String> names = Tag.getTagNameCollectionFrom(tagNames);
		final Set<Tag> output = new HashSet<Tag>();
		for (String name : names) {
			Tag t = Tag.getTagFromName(name, existingTags);
			
			boolean weHaveNewTag = false;
			if (t == null) {
				t = new Tag(name);
				weHaveNewTag = true;
			}
			
			saveTag(t);
			
			if (weHaveNewTag) {
				ViewUtil.addMessage("Creating", "Tag : " + t.getTagName(), null);
			}

			output.add(t);
		}
		return output;
	}
	
	public void saveTag(TagBean tb, TagListBean tagListBean) {
		Tag t = tb.getTag();
		if (t.getId() == null) {
			// a new tag
			// [TODO] check name collision with the existing tags
			saveTag(t);
			ViewUtil.addMessage("Creating", "Tag : " + t.getTagName(), null);
			
		}
		else {
			// an existing tag
			final String newName = t.getTagName();
			t = Tag.getTagFromId(t.getId(), tagListBean.getTags());
			final String oldName = t.getTagName();
			t.setTagName(newName);
			saveTag(t);
			ViewUtil.addMessage("Renaming", "Tag : " + oldName + " => " + newName, null);
		}
	}
	
	public void trashTag(TagBean tb) {
		final Tag t = tb.getTag();
		final Long id = t.getId();
		if (id != null) {
			final List<Long> fids = fragmentDao.findIdsByTagId(id);
			trashFragments(fids);
		}
	}

	public void deleteTag(TagBean tb) {
		Tag t = tb.getTag();
		final Long id = t.getId();
		if (id != null) {
			t = tagDao.findById(id);
			tagDao.delete(t);
			ViewUtil.addMessage("Deleting", "Tag : " + t.getTagName(), null);
		}
	}

	public FragmentBean inspectFragment(Integer index, FragmentListBean flb) {
		final FragmentBean fb = flb.getFragmentBeanAt(index);
	    logger.info("inspectFragment() called");
	    return fb;
	}


}
