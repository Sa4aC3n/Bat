import React, { useState, useEffect } from 'react';
import { 
  Lock, 
  Unlock, 
  Users, 
  ListTodo, 
  ShieldCheck, 
  CheckCircle, 
  RefreshCw, 
  Plus, 
  X, 
  Archive, 
  Sparkles, 
  Check, 
  AlertCircle 
} from 'lucide-react';
import confetti from 'canvas-confetti';

export interface WebChild {
  id: string;
  alias: string;
  ageGroup: '4-6' | '7-9' | '10-12';
  avatarId: string;
  isArchived: boolean;
}

export interface WebParentTask {
  id: string;
  title: string;
  description: string;
  requiresApproval: boolean;
  recurrence: 'daily' | 'weekly' | 'once';
  assignedChildIds: string[];
  isArchived: boolean;
}

export interface WebTaskOccurrence {
  id: string;
  taskId: string;
  childId: string;
  dateStr: string;
  status: 'NOT_STARTED' | 'PENDING_APPROVAL' | 'COMPLETED' | 'SKIPPED';
  snapshotTitle: string;
  snapshotDescription: string;
  requiresApproval: boolean;
  parentFeedbackNote?: string;
}

interface ParentsSectionModalProps {
  isOpen: boolean;
  onClose: () => void;
  childrenList: WebChild[];
  setChildrenList: React.Dispatch<React.SetStateAction<WebChild[]>>;
  parentTasks: WebParentTask[];
  setParentTasks: React.Dispatch<React.SetStateAction<WebParentTask[]>>;
  occurrences: WebTaskOccurrence[];
  setOccurrences: React.Dispatch<React.SetStateAction<WebTaskOccurrence[]>>;
  onPraise: (msg: string) => void;
}

export const PRAISE_MESSAGES = [
  'محاولة ممتازة تدل على إصرارك النبيل!',
  'كل خطوة تخطوها تصنع منك بطلاً حقيقيًا!',
  'فخورون بعطائك واجتهادك الصادق اليوم!',
  'الإنجاز الحقيقي يبدأ بالاستمرار والمحاولة!',
  'أثرك الطيب يترك بصمة نور في بيتنا!'
];

export default function ParentsSectionModal({
  isOpen,
  onClose,
  childrenList,
  setChildrenList,
  parentTasks,
  setParentTasks,
  occurrences,
  setOccurrences,
  onPraise
}: ParentsSectionModalProps) {
  // PIN state
  const [pinConfigured, setPinConfigured] = useState<boolean>(() => {
    return !!localStorage.getItem('hero_parent_pin');
  });
  const [isUnlocked, setIsUnlocked] = useState(false);
  const [pinInput, setPinInput] = useState('');
  const [setupStep, setSetupStep] = useState<1 | 2>(1);
  const [firstAttemptPin, setFirstAttemptPin] = useState('');
  const [pinError, setPinError] = useState('');

  // Tabs in parents section
  const [activeSubTab, setActiveSubTab] = useState<'children' | 'tasks' | 'approvals' | 'security'>('children');

  // Child Form Modal
  const [showAddChild, setShowAddChild] = useState(false);
  const [childAlias, setChildAlias] = useState('');
  const [childAgeGroup, setChildAgeGroup] = useState<'4-6' | '7-9' | '10-12'>('7-9');

  // Task Form Modal
  const [showAddTask, setShowAddTask] = useState(false);
  const [taskTitle, setTaskTitle] = useState('');
  const [taskDesc, setTaskDesc] = useState('');
  const [taskReqApproval, setTaskReqApproval] = useState(true);
  const [taskRecurrence, setTaskRecurrence] = useState<'daily' | 'weekly' | 'once'>('daily');
  const [selectedKidsForTask, setSelectedKidsForTask] = useState<string[]>([]);

  // Retry feedback modal
  const [retryingOccId, setRetryingOccId] = useState<string | null>(null);
  const [gentleNote, setGentleNote] = useState('');

  // Change PIN modal
  const [showChangePin, setShowChangePin] = useState(false);
  const [currPin, setCurrPin] = useState('');
  const [newPin, setNewPin] = useState('');
  const [confirmNewPin, setConfirmNewPin] = useState('');

  if (!isOpen) return null;

  // Handle PIN keypad / typing
  const handlePinDigit = (digit: string) => {
    if (pinInput.length < 6) {
      const next = pinInput + digit;
      setPinInput(next);
      setPinError('');

      if (next.length === 6) {
        if (!pinConfigured) {
          if (setupStep === 1) {
            setFirstAttemptPin(next);
            setPinInput('');
            setSetupStep(2);
          } else {
            if (next === firstAttemptPin) {
              localStorage.setItem('hero_parent_pin', next);
              setPinConfigured(true);
              setIsUnlocked(true);
              setPinInput('');
            } else {
              setPinError('الرمزان غير متطابقين. يرجى البدء من جديد.');
              setSetupStep(1);
              setPinInput('');
            }
          }
        } else {
          const savedPin = localStorage.getItem('hero_parent_pin') || '123456';
          if (next === savedPin) {
            setIsUnlocked(true);
            setPinInput('');
          } else {
            setPinError('رمز غير صحيح! حاول مجددًا');
            setPinInput('');
          }
        }
      }
    }
  };

  const handleDeleteDigit = () => {
    setPinInput(prev => prev.slice(0, -1));
    setPinError('');
  };

  // Add child
  const handleSaveChild = (e: React.FormEvent) => {
    e.preventDefault();
    if (!childAlias.trim()) return;
    const newChild: WebChild = {
      id: Date.now().toString(),
      alias: childAlias.trim(),
      ageGroup: childAgeGroup,
      avatarId: 'star',
      isArchived: false
    };
    setChildrenList(prev => [...prev, newChild]);
    setChildAlias('');
    setShowAddChild(false);
  };

  // Add Task
  const handleSaveTask = (e: React.FormEvent) => {
    e.preventDefault();
    if (!taskTitle.trim() || selectedKidsForTask.length === 0) return;
    const newTask: WebParentTask = {
      id: Date.now().toString(),
      title: taskTitle.trim(),
      description: taskDesc.trim(),
      requiresApproval: taskReqApproval,
      recurrence: taskRecurrence,
      assignedChildIds: selectedKidsForTask,
      isArchived: false
    };
    setParentTasks(prev => [...prev, newTask]);

    // Create today occurrence for each assigned child
    const todayStr = new Date().toISOString().split('T')[0];
    const newOccs: WebTaskOccurrence[] = selectedKidsForTask.map(kId => ({
      id: `${newTask.id}_${kId}_${todayStr}`,
      taskId: newTask.id,
      childId: kId,
      dateStr: todayStr,
      status: 'NOT_STARTED',
      snapshotTitle: newTask.title,
      snapshotDescription: newTask.description,
      requiresApproval: newTask.requiresApproval
    }));

    setOccurrences(prev => [...prev, ...newOccs]);
    setTaskTitle('');
    setTaskDesc('');
    setShowAddTask(false);
  };

  // Approve Occurrence
  const handleApprove = (occId: string) => {
    setOccurrences(prev => prev.map(o => o.id === occId ? { ...o, status: 'COMPLETED' } : o));
    const praise = PRAISE_MESSAGES[Math.floor(Math.random() * PRAISE_MESSAGES.length)];
    onPraise(praise);
    confetti({ particleCount: 50, spread: 60, origin: { y: 0.6 } });
  };

  // Retry Occurrence
  const handleRetry = () => {
    if (!retryingOccId) return;
    setOccurrences(prev => prev.map(o => o.id === retryingOccId ? {
      ...o,
      status: 'NOT_STARTED',
      parentFeedbackNote: gentleNote.trim() || 'محاولة جيدة، هيا نجرب معًا بهدوء'
    } : o));
    setRetryingOccId(null);
    setGentleNote('');
  };

  const pendingApprovals = occurrences.filter(o => o.status === 'PENDING_APPROVAL');

  return (
    <div className="fixed inset-0 z-50 bg-slate-950/85 backdrop-blur-md flex items-center justify-center p-4">
      <div className="bg-slate-900 border border-slate-700 w-full max-w-2xl rounded-3xl overflow-hidden shadow-2xl flex flex-col max-h-[90vh]">
        
        {/* Header */}
        <div className="p-4 sm:p-5 bg-slate-800/80 border-b border-slate-700 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-amber-500/20 text-amber-400 flex items-center justify-center">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-slate-100 text-base sm:text-lg">قسم الوالدين والأمان</h3>
              <p className="text-xs text-slate-400">إدارة الأبطال الصغار والمهام ومراجعة الإنجازات بخصوصية تامة</p>
            </div>
          </div>
          <button 
            onClick={onClose}
            className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-slate-200 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content: If Locked -> Show PIN keypad */}
        {!isUnlocked ? (
          <div className="p-6 sm:p-8 flex flex-col items-center justify-center text-center space-y-4">
            <div className="w-16 h-16 rounded-2xl bg-amber-500/10 border border-amber-500/30 flex items-center justify-center text-amber-400">
              <Lock className="w-8 h-8" />
            </div>

            <div className="space-y-1 max-w-sm">
              <h4 className="font-bold text-lg text-slate-100">
                {!pinConfigured && setupStep === 1 && 'تعيين رمز سري للأهل (٦ أرقام)'}
                {!pinConfigured && setupStep === 2 && 'تأكيد الرمز السري'}
                {pinConfigured && 'أدخل رمز المرور السري (٦ أرقام)'}
              </h4>
              <p className="text-xs text-slate-400">
                يتم حفظ الرمز محليًا على هذا الجهاز لحماية خصوصية الأطفال ومهامهم دون إرسال أي بيانات لخوادم خارجية.
              </p>
            </div>

            {/* PIN Dots */}
            <div className="flex items-center gap-3 py-2">
              {[0, 1, 2, 3, 4, 5].map(idx => (
                <div 
                  key={idx} 
                  className={`w-4 h-4 rounded-full border transition-all ${
                    idx < pinInput.length 
                      ? 'bg-amber-400 border-amber-400 scale-110 shadow-sm shadow-amber-400/50' 
                      : 'border-slate-600 bg-slate-800'
                  }`}
                />
              ))}
            </div>

            {pinError && (
              <p className="text-xs font-bold text-rose-400 animate-shake">{pinError}</p>
            )}

            {/* Keypad */}
            <div className="grid grid-cols-3 gap-2 w-full max-w-[240px] pt-2">
              {['1', '2', '3', '4', '5', '6', '7', '8', '9'].map(num => (
                <button
                  key={num}
                  onClick={() => handlePinDigit(num)}
                  className="h-12 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-100 font-bold text-lg border border-slate-700/60 active:scale-95 transition"
                >
                  {num}
                </button>
              ))}
              <div />
              <button
                onClick={() => handlePinDigit('0')}
                className="h-12 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-100 font-bold text-lg border border-slate-700/60 active:scale-95 transition"
              >
                0
              </button>
              <button
                onClick={handleDeleteDigit}
                className="h-12 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-300 font-bold text-sm border border-slate-700/60 active:scale-95 transition flex items-center justify-center"
              >
                مسح
              </button>
            </div>
          </div>
        ) : (
          /* Unlocked Parent Dashboard */
          <div className="flex-1 flex flex-col overflow-hidden">
            
            {/* Tabs */}
            <div className="flex items-center border-b border-slate-800 px-4 bg-slate-850 overflow-x-auto">
              {[
                { id: 'children', label: 'الأطفال', icon: Users, count: childrenList.filter(c => !c.isArchived).length },
                { id: 'tasks', label: 'إدارة المهام', icon: ListTodo, count: parentTasks.filter(t => !t.isArchived).length },
                { id: 'approvals', label: 'طلبات الاعتماد', icon: CheckCircle, count: pendingApprovals.length },
                { id: 'security', label: 'الأمان والرمز', icon: ShieldCheck }
              ].map(t => {
                const Icon = t.icon;
                const isCur = activeSubTab === t.id;
                return (
                  <button
                    key={t.id}
                    onClick={() => setActiveSubTab(t.id as any)}
                    className={`flex items-center gap-2 py-3 px-4 text-xs font-bold border-b-2 transition whitespace-nowrap ${
                      isCur ? 'border-amber-400 text-amber-400' : 'border-transparent text-slate-400 hover:text-slate-200'
                    }`}
                  >
                    <Icon className="w-4 h-4" />
                    <span>{t.label}</span>
                    {t.count !== undefined && t.count > 0 && (
                      <span className={`px-1.5 py-0.2 rounded-full text-[10px] font-black ${
                        t.id === 'approvals' ? 'bg-amber-500 text-slate-950' : 'bg-slate-700 text-slate-300'
                      }`}>
                        {t.count}
                      </span>
                    )}
                  </button>
                );
              })}
            </div>

            {/* Tab Views */}
            <div className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-4">
              
              {/* TAB 1: CHILDREN */}
              {activeSubTab === 'children' && (
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <h4 className="font-bold text-sm text-slate-200">ملفات الأبطال الصغار</h4>
                    <button
                      onClick={() => setShowAddChild(true)}
                      className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold text-xs transition"
                    >
                      <Plus className="w-3.5 h-3.5" />
                      <span>إضافة طفل</span>
                    </button>
                  </div>

                  {childrenList.filter(c => !c.isArchived).length === 0 ? (
                    <div className="p-8 text-center bg-slate-800/40 border border-slate-700/60 rounded-2xl space-y-2">
                      <Users className="w-10 h-10 text-amber-400/60 mx-auto" />
                      <p className="text-sm font-bold text-slate-300">لم تتم إضافة أي أطفال بعد</p>
                      <p className="text-xs text-slate-400">أضف أبطالك الصغار باسم مستعار وفئة عمرية لتخصيص مهامهم ومتابعة إنجازاتهم.</p>
                    </div>
                  ) : (
                    <div className="grid gap-3 sm:grid-cols-2">
                      {childrenList.filter(c => !c.isArchived).map(child => (
                        <div key={child.id} className="p-4 rounded-2xl bg-slate-800/60 border border-slate-700 flex items-center justify-between">
                          <div className="flex items-center gap-3">
                            <div className="w-10 h-10 rounded-full bg-amber-500/20 text-amber-400 flex items-center justify-center font-bold">
                              ★
                            </div>
                            <div>
                              <p className="font-bold text-slate-100 text-sm">{child.alias}</p>
                              <p className="text-xs text-slate-400">الفئة: {child.ageGroup} سنوات</p>
                            </div>
                          </div>
                          <button
                            onClick={() => {
                              if (confirm(`هل تريد أرشفة ملف «${child.alias}»؟ ستبقى إنجازاته محفوظة في السجل.`)) {
                                setChildrenList(prev => prev.map(c => c.id === child.id ? { ...c, isArchived: true } : c));
                              }
                            }}
                            className="p-1.5 text-slate-400 hover:text-rose-400 transition"
                            title="أرشفة"
                          >
                            <Archive className="w-4 h-4" />
                          </button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {/* TAB 2: TASKS */}
              {activeSubTab === 'tasks' && (
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <h4 className="font-bold text-sm text-slate-200">المهام اليومية والأسبوعية</h4>
                    <button
                      onClick={() => {
                        setSelectedKidsForTask(childrenList.filter(c => !c.isArchived).map(c => c.id));
                        setShowAddTask(true);
                      }}
                      className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold text-xs transition"
                    >
                      <Plus className="w-3.5 h-3.5" />
                      <span>إنشاء مهمة</span>
                    </button>
                  </div>

                  {parentTasks.filter(t => !t.isArchived).length === 0 ? (
                    <div className="p-8 text-center bg-slate-800/40 border border-slate-700/60 rounded-2xl space-y-2">
                      <ListTodo className="w-10 h-10 text-amber-400/60 mx-auto" />
                      <p className="text-sm font-bold text-slate-300">لا توجد مهام حالية</p>
                      <p className="text-xs text-slate-400">أنشئ مهامًا مخصصة للأبطال مثل ترتيب الغرفة أو مساعدة الإخوة أو القراءة.</p>
                    </div>
                  ) : (
                    <div className="space-y-3">
                      {parentTasks.filter(t => !t.isArchived).map(task => (
                        <div key={task.id} className="p-4 rounded-2xl bg-slate-800/60 border border-slate-700 flex items-start justify-between gap-3">
                          <div>
                            <p className="font-bold text-slate-100 text-sm">{task.title}</p>
                            {task.description && (
                              <p className="text-xs text-slate-400 mt-0.5">{task.description}</p>
                            )}
                            <div className="flex flex-wrap gap-2 pt-2">
                              <span className="text-[10px] font-bold px-2 py-0.5 rounded-md bg-amber-500/10 text-amber-300 border border-amber-500/20">
                                {task.recurrence === 'daily' ? 'يوميًا' : task.recurrence === 'weekly' ? 'أسبوعيًا' : 'مرة واحدة'}
                              </span>
                              {task.requiresApproval && (
                                <span className="text-[10px] font-bold px-2 py-0.5 rounded-md bg-blue-500/10 text-blue-300 border border-blue-500/20">
                                  تتطلب موافقة الوالدين
                                </span>
                              )}
                            </div>
                          </div>
                          <button
                            onClick={() => {
                              if (confirm(`هل تريد أرشفة مهمة «${task.title}»؟ لن تظهر مجددًا في الأيام القادمة.`)) {
                                setParentTasks(prev => prev.map(t => t.id === task.id ? { ...t, isArchived: true } : t));
                              }
                            }}
                            className="p-1.5 text-slate-400 hover:text-rose-400 transition"
                            title="أرشفة"
                          >
                            <Archive className="w-4 h-4" />
                          </button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {/* TAB 3: APPROVALS */}
              {activeSubTab === 'approvals' && (
                <div className="space-y-4">
                  <h4 className="font-bold text-sm text-slate-200">طلبات التأكيد والاعتماد</h4>
                  {pendingApprovals.length === 0 ? (
                    <div className="p-8 text-center bg-slate-800/40 border border-slate-700/60 rounded-2xl space-y-2">
                      <CheckCircle className="w-10 h-10 text-emerald-400/60 mx-auto" />
                      <p className="text-sm font-bold text-slate-300">لا توجد طلبات بانتظار الاعتماد</p>
                      <p className="text-xs text-slate-400">عندما يُنجز أحد الأطفال مهمة تتطلب تأكيدًا، ستظهر هنا لمراجعتها وتشجيعه.</p>
                    </div>
                  ) : (
                    <div className="space-y-3">
                      {pendingApprovals.map(occ => {
                        const child = childrenList.find(c => c.id === occ.childId);
                        return (
                          <div key={occ.id} className="p-4 rounded-2xl bg-slate-800 border border-amber-500/30 space-y-3">
                            <div className="flex items-center justify-between">
                              <span className="font-bold text-sm text-slate-100">{occ.snapshotTitle}</span>
                              <span className="text-xs font-bold text-amber-400 bg-amber-500/10 px-2.5 py-0.5 rounded-full">
                                {child?.alias || 'البطل'}
                              </span>
                            </div>
                            {occ.snapshotDescription && (
                              <p className="text-xs text-slate-400">{occ.snapshotDescription}</p>
                            )}
                            <div className="flex items-center gap-2 pt-1">
                              <button
                                onClick={() => handleApprove(occ.id)}
                                className="flex-1 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-slate-100 font-bold text-xs flex items-center justify-center gap-1.5 transition"
                              >
                                <Check className="w-4 h-4" />
                                <span>تأكيد الإنجاز</span>
                              </button>
                              <button
                                onClick={() => setRetryingOccId(occ.id)}
                                className="flex-1 py-2 rounded-xl bg-slate-700 hover:bg-slate-650 text-slate-200 font-bold text-xs flex items-center justify-center gap-1.5 transition"
                              >
                                <RefreshCw className="w-4 h-4" />
                                <span>نحاول مرة تانية</span>
                              </button>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              )}

              {/* TAB 4: SECURITY */}
              {activeSubTab === 'security' && (
                <div className="space-y-4">
                  <div className="p-4 rounded-2xl bg-slate-800/60 border border-slate-700 space-y-2">
                    <h5 className="font-bold text-sm text-slate-100 flex items-center gap-2">
                      <ShieldCheck className="w-4 h-4 text-amber-400" />
                      الخصوصية والأمان الأسري
                    </h5>
                    <p className="text-xs text-slate-400 leading-relaxed">
                      • جميع بيانات الأطفال والمهام والرمز السري محفوظة محليًا تمامًا على جهازك.<br />
                      • لا نجمع بيانات شخصية ولا نتتبع أي نشاط إلكتروني.<br />
                      • يمكنك تغيير رمز الدخول متى شئت.
                    </p>
                  </div>

                  <div className="flex flex-col gap-2 pt-2">
                    <button
                      onClick={() => setShowChangePin(true)}
                      className="w-full py-3 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-200 font-bold text-xs border border-slate-700 transition"
                    >
                      تغيير رمز المرور السري (٦ أرقام)
                    </button>
                    <button
                      onClick={() => {
                        setIsUnlocked(false);
                        onClose();
                      }}
                      className="w-full py-3 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 text-rose-300 font-bold text-xs border border-rose-500/20 transition"
                    >
                      قفل قسم الوالدين الآن
                    </button>
                  </div>
                </div>
              )}

            </div>
          </div>
        )}
      </div>

      {/* Add Child Dialog */}
      {showAddChild && (
        <div className="fixed inset-0 z-60 bg-slate-950/80 flex items-center justify-center p-4">
          <form onSubmit={handleSaveChild} className="bg-slate-900 border border-slate-700 rounded-3xl p-6 w-full max-w-sm space-y-4">
            <h4 className="font-bold text-base text-slate-100">إضافة بطل صغير</h4>
            <div>
              <label className="block text-xs text-slate-400 mb-1">الاسم المستعار (دون معلومات شخصية):</label>
              <input
                type="text"
                required
                placeholder="مثال: أحمد، بطل القراءة..."
                value={childAlias}
                onChange={e => setChildAlias(e.target.value)}
                className="w-full px-3 py-2 rounded-xl bg-slate-800 border border-slate-700 text-slate-100 text-sm focus:border-amber-400 outline-none"
              />
            </div>
            <div>
              <label className="block text-xs text-slate-400 mb-1">الفئة العمرية:</label>
              <div className="grid grid-cols-3 gap-2">
                {(['4-6', '7-9', '10-12'] as const).map(ag => (
                  <button
                    type="button"
                    key={ag}
                    onClick={() => setChildAgeGroup(ag)}
                    className={`py-2 rounded-xl text-xs font-bold border transition ${
                      childAgeGroup === ag ? 'bg-amber-500 text-slate-950 border-amber-400' : 'bg-slate-800 text-slate-300 border-slate-700'
                    }`}
                  >
                    {ag} سنوات
                  </button>
                ))}
              </div>
            </div>
            <div className="flex items-center gap-2 pt-2">
              <button
                type="submit"
                className="flex-1 py-2.5 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold text-xs transition"
              >
                حفظ الطفل
              </button>
              <button
                type="button"
                onClick={() => setShowAddChild(false)}
                className="flex-1 py-2.5 rounded-xl bg-slate-800 text-slate-300 font-bold text-xs hover:bg-slate-700 transition"
              >
                إلغاء
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Add Task Dialog */}
      {showAddTask && (
        <div className="fixed inset-0 z-60 bg-slate-950/80 flex items-center justify-center p-4">
          <form onSubmit={handleSaveTask} className="bg-slate-900 border border-slate-700 rounded-3xl p-6 w-full max-w-md space-y-4 max-h-[90vh] overflow-y-auto">
            <h4 className="font-bold text-base text-slate-100">إنشاء مهمة جديدة للأبطال</h4>
            <div>
              <label className="block text-xs text-slate-400 mb-1">عنوان المهمة:</label>
              <input
                type="text"
                required
                placeholder="مثال: ترتيب السرير، قراءة صفحتين..."
                value={taskTitle}
                onChange={e => setTaskTitle(e.target.value)}
                className="w-full px-3 py-2 rounded-xl bg-slate-800 border border-slate-700 text-slate-100 text-sm focus:border-amber-400 outline-none"
              />
            </div>
            <div>
              <label className="block text-xs text-slate-400 mb-1">الوصف والتشجيع:</label>
              <input
                type="text"
                placeholder="مثال: رتب غطاءك لبدء يوم منظم ومريح"
                value={taskDesc}
                onChange={e => setTaskDesc(e.target.value)}
                className="w-full px-3 py-2 rounded-xl bg-slate-800 border border-slate-700 text-slate-100 text-sm focus:border-amber-400 outline-none"
              />
            </div>
            <div>
              <label className="block text-xs text-slate-400 mb-1">تكرار المهمة:</label>
              <div className="grid grid-cols-3 gap-2">
                {[
                  { id: 'daily', label: 'يوميًا' },
                  { id: 'weekly', label: 'أسبوعيًا' },
                  { id: 'once', label: 'مرة واحدة' }
                ].map(r => (
                  <button
                    type="button"
                    key={r.id}
                    onClick={() => setTaskRecurrence(r.id as any)}
                    className={`py-2 rounded-xl text-xs font-bold border transition ${
                      taskRecurrence === r.id ? 'bg-amber-500 text-slate-950 border-amber-400' : 'bg-slate-800 text-slate-300 border-slate-700'
                    }`}
                  >
                    {r.label}
                  </button>
                ))}
              </div>
            </div>
            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-800 border border-slate-700">
              <div>
                <p className="text-xs font-bold text-slate-200">تتطلب موافقة الوالدين</p>
                <p className="text-[11px] text-slate-400">لا تُعتبر مكتملة حتى يؤكدها ولي الأمر</p>
              </div>
              <input
                type="checkbox"
                checked={taskReqApproval}
                onChange={e => setTaskReqApproval(e.target.checked)}
                className="w-5 h-5 accent-amber-500 cursor-pointer"
              />
            </div>
            <div>
              <label className="block text-xs text-slate-400 mb-1">تعيين للأطفال:</label>
              <div className="space-y-1">
                {childrenList.filter(c => !c.isArchived).map(c => {
                  const checked = selectedKidsForTask.includes(c.id);
                  return (
                    <label key={c.id} className="flex items-center gap-2 p-2 rounded-xl bg-slate-800/60 cursor-pointer">
                      <input
                        type="checkbox"
                        checked={checked}
                        onChange={e => {
                          if (e.target.checked) setSelectedKidsForTask(prev => [...prev, c.id]);
                          else setSelectedKidsForTask(prev => prev.filter(k => k !== c.id));
                        }}
                        className="w-4 h-4 accent-amber-500"
                      />
                      <span className="text-xs text-slate-200">{c.alias}</span>
                    </label>
                  );
                })}
              </div>
            </div>
            <div className="flex items-center gap-2 pt-2">
              <button
                type="submit"
                className="flex-1 py-2.5 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold text-xs transition"
              >
                إنشاء
              </button>
              <button
                type="button"
                onClick={() => setShowAddTask(false)}
                className="flex-1 py-2.5 rounded-xl bg-slate-800 text-slate-300 font-bold text-xs hover:bg-slate-700 transition"
              >
                إلغاء
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Gentle Retry Feedback Note Dialog */}
      {retryingOccId && (
        <div className="fixed inset-0 z-60 bg-slate-950/80 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-3xl p-6 w-full max-w-sm space-y-4">
            <h4 className="font-bold text-base text-slate-100">طلب إعادة المحاولة بلطف</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              ستعود المهمة لحالة «لم تبدأ» مع ملاحظة تشجيعية دافئة لمساعدة بطلك الصغير.
            </p>
            <input
              type="text"
              placeholder="مثال: محاولة رائعة، تعال نرتب الألعاب معًا"
              value={gentleNote}
              onChange={e => setGentleNote(e.target.value)}
              className="w-full px-3 py-2 rounded-xl bg-slate-800 border border-slate-700 text-slate-100 text-sm focus:border-amber-400 outline-none"
            />
            <div className="flex items-center gap-2 pt-2">
              <button
                onClick={handleRetry}
                className="flex-1 py-2.5 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold text-xs transition"
              >
                إرسال بلطف
              </button>
              <button
                onClick={() => setRetryingOccId(null)}
                className="flex-1 py-2.5 rounded-xl bg-slate-800 text-slate-300 font-bold text-xs hover:bg-slate-700 transition"
              >
                إلغاء
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Change PIN Dialog */}
      {showChangePin && (
        <div className="fixed inset-0 z-60 bg-slate-950/80 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-3xl p-6 w-full max-w-sm space-y-3">
            <h4 className="font-bold text-base text-slate-100">تغيير رمز المرور</h4>
            <input
              type="password"
              maxLength={6}
              placeholder="الرمز الحالي (٦ أرقام)"
              value={currPin}
              onChange={e => setCurrPin(e.target.value)}
              className="w-full px-3 py-2 rounded-xl bg-slate-800 border border-slate-700 text-slate-100 text-sm focus:border-amber-400 outline-none"
            />
            <input
              type="password"
              maxLength={6}
              placeholder="الرمز الجديد (٦ أرقام)"
              value={newPin}
              onChange={e => setNewPin(e.target.value)}
              className="w-full px-3 py-2 rounded-xl bg-slate-800 border border-slate-700 text-slate-100 text-sm focus:border-amber-400 outline-none"
            />
            <input
              type="password"
              maxLength={6}
              placeholder="تأكيد الرمز الجديد"
              value={confirmNewPin}
              onChange={e => setConfirmNewPin(e.target.value)}
              className="w-full px-3 py-2 rounded-xl bg-slate-800 border border-slate-700 text-slate-100 text-sm focus:border-amber-400 outline-none"
            />
            <div className="flex items-center gap-2 pt-2">
              <button
                onClick={() => {
                  const saved = localStorage.getItem('hero_parent_pin') || '123456';
                  if (currPin !== saved) {
                    alert('الرمز الحالي غير صحيح');
                    return;
                  }
                  if (newPin.length !== 6 || newPin !== confirmNewPin) {
                    alert('الرمز الجديد غير صالح أو غير متطابق');
                    return;
                  }
                  localStorage.setItem('hero_parent_pin', newPin);
                  alert('تم تغيير الرمز بنجاح!');
                  setShowChangePin(false);
                }}
                className="flex-1 py-2.5 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold text-xs transition"
              >
                حفظ
              </button>
              <button
                onClick={() => setShowChangePin(false)}
                className="flex-1 py-2.5 rounded-xl bg-slate-800 text-slate-300 font-bold text-xs hover:bg-slate-700 transition"
              >
                إلغاء
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
