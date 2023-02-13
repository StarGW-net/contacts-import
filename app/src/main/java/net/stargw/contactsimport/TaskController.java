package net.stargw.contactsimport;

import android.content.Context;


public class TaskController {


	TaskImport myTaskImport;
	TaskDeleteAll myTaskDelete;
	TaskBackup myTaskBackup;
	TaskExport myTaskExport;
	TaskRecover myTaskRecover;
	TaskDeleteContacts myTaskDeleteContacts;
	TaskDeleteGroups myTaskDeleteGroups;
	TaskLoadCSV myTaskLoadCSV;
	TaskGetContactsDelete myTaskGetContactsDelete;
	TaskGetContactsView myTaskGetContactsView;
	// TaskGetLogs myTaskGetLogs;
	
	//
	// Controls the sequential execution of async tasks
	//

		boolean add = false;
		boolean del = false;
		boolean backup = false;
		boolean export = false;
		boolean exportFull = false;
		boolean recover = false;
		boolean delContacts = false;
		boolean delGroups = false;
		boolean loadCSV = false;
		boolean getContactsDelete = false;
		boolean getContactsView = false;
		boolean getLogs = false;
		
		Context context;
		
		public TaskController()
		{
			cancelTasks();
		}

		
		public void cancelTasks()
		{
			add = false;
			del = false;
			backup = false;
			export = false;
			exportFull = false;
			recover = false;
			delContacts = false;
			delGroups = false;
			loadCSV = false;
			getContactsDelete = false;
			getContactsView = false;
			getLogs = false;
			Global.setTaskText(null);
		}
		
		public void setTaskRecover(boolean val)
		{
			recover = val;
		}
		
		public void setTaskImport(boolean val)
		{
			add = val;
		}
		
		public void setTaskDelete(boolean val)
		{
			del = val;
		}
		
		public void setTaskDeleteContacts(boolean val)
		{
			delContacts = val;
		}
		
		public void setTaskDeleteGroups(boolean val)
		{
			delGroups = val;
		}
		
		public void setTaskBackup(boolean val)
		{
			backup = val;
		}
		
		public void setTaskExport(boolean val)
		{
			export = val;
		}
		
		public void setTaskLoadCSV(boolean val)
		{
			loadCSV = val;
		}
		
		public void setTaskGetContactsDelete(boolean val)
		{
			getContactsDelete = val;
		}
		
		public void setTaskGetContactsView(boolean val)
		{
			getContactsView = val;
		}
		public void setTaskGetLogs(boolean val)
		{
			getLogs = val;
		}
		
		public void nextTask()
		{
			if (getContactsDelete == true)
			{
				myTaskGetContactsDelete.execute();
				return;
			}
			if (getLogs == true)
			{
				// myTaskGetLogs.execute();
				return;
			}
			if (getContactsView == true)
			{
				myTaskGetContactsView.execute();
				return;
			}
			if (backup == true)
			{
				Global.setTaskText(Global.getContext().getString(R.string.backingup));
				myTaskBackup.execute();
				return;
			}
			if (delContacts == true)
			{
				Global.setTaskText(Global.getContext().getString(R.string.deleting));
				myTaskDeleteContacts.execute();
				return;
			}
			if (delGroups == true)
			{
				Global.setTaskText(Global.getContext().getString(R.string.deletingGroups));
				myTaskDeleteGroups.execute();
				return;
			}
			if (del == true)
			{
				Global.setTaskText(Global.getContext().getString(R.string.deleting));
				myTaskDelete.execute();
				return;
			}
			
			if (add == true)
			{
				Global.setTaskText(Global.getContext().getString(R.string.importing));
				myTaskImport.execute();
				return;
			}
			
			if (recover == true)
			{
				Global.setTaskText(Global.getContext().getString(R.string.recovering));
				myTaskRecover.execute();
				return;
			}
			if (loadCSV == true)
			{
				// myTaskRecover = new TaskRecover(this,context);
				myTaskLoadCSV.execute();
				return;
			}
			if (export == true)
			{
				Global.setTaskText(Global.getContext().getString(R.string.exporting));
				myTaskExport.execute();
				return;
			}
			Global.setTaskText(null);
		}
		
		public boolean lastTask()
		{
			if (backup == true)
			{
				return false;
			}
			if (getLogs == true)
			{
				return false;
			}
			if (del == true)
			{
				return false;
			}
			
			if (add == true)
			{
				return false;
			}
			if (export == true)
			{
				return false;
			}
			if (recover == true)
			{
				return false;
			}
			if (delContacts == true)
			{
				return false;
			}
			if (delGroups == true)
			{
				return false;
			}
			if (loadCSV == true)
			{
				return false;
			}
			if (getContactsDelete == true)
			{
				return false;
			}
			if (getContactsView == true)
			{
				return false;
			}

			if (Global.isActivityVisible() == false)
			{
				Global.activityDone();
			}
			Global.setTaskText(null);
			return true;
		}
		// decide which task to run first.
		
		// when a task is finished it calls this back
		
	}

