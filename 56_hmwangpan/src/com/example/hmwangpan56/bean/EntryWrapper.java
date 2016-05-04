package com.example.hmwangpan56.bean;

import com.vdisk.net.VDiskAPI.Entry;

public class EntryWrapper {
	public Entry entry;//
	public boolean isCheck;//记录当前的EntryWrapper是否被选中

	@Override
	public int hashCode() {
		if (this.entry != null) {
			return this.entry.path.hashCode();
		}
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EntryWrapper) {
			EntryWrapper entryWrapper = (EntryWrapper) obj;
			if (entryWrapper.entry != null && this.entry != null) {
				if (entryWrapper.entry.path.equals(this.entry.path)) {
					return true;
				}
			}
		}
		return false;
	}
}
