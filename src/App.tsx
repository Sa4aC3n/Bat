import React, { useState, useEffect } from 'react';
import { 
  Award, 
  CheckCircle2, 
  HeartHandshake, 
  BookOpen, 
  Sparkles, 
  Share2, 
  Bookmark, 
  BookmarkCheck, 
  Shuffle, 
  Flame, 
  Star, 
  Trophy, 
  Quote, 
  PlusCircle, 
  Trash2, 
  Search, 
  HelpCircle, 
  RotateCcw,
  Check,
  Stethoscope,
  GraduationCap,
  Compass,
  Binary,
  Eye,
  ShieldAlert,
  Heart,
  ChevronLeft,
  Lock,
  User,
  Undo2,
  Hourglass
} from 'lucide-react';
import confetti from 'canvas-confetti';
import { HEROES, DEFAULT_DEEDS, QUIZ_QUESTIONS, Hero, HonoredHero } from './data/heroes';
import ParentsSectionModal, { WebChild, WebParentTask, WebTaskOccurrence } from './ParentsSectionModal';

export default function App() {
  const [activeTab, setActiveTab] = useState<'today' | 'challenge' | 'honor' | 'archive'>('today');

  // Parents section & children in localStorage
  const [showParentsModal, setShowParentsModal] = useState(false);
  const [effortPraise, setEffortPraise] = useState<string | null>(null);

  const [childrenList, setChildrenList] = useState<WebChild[]>(() => {
    try {
      const saved = localStorage.getItem('hero_children_list');
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  const [selectedChildId, setSelectedChildId] = useState<string>(() => {
    return localStorage.getItem('hero_selected_child_id') || '';
  });

  const [parentTasks, setParentTasks] = useState<WebParentTask[]>(() => {
    try {
      const saved = localStorage.getItem('hero_parent_tasks');
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  const [taskOccurrences, setTaskOccurrences] = useState<WebTaskOccurrence[]>(() => {
    try {
      const saved = localStorage.getItem('hero_task_occurrences');
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  useEffect(() => {
    localStorage.setItem('hero_children_list', JSON.stringify(childrenList));
    if (!selectedChildId && childrenList.length > 0) {
      setSelectedChildId(childrenList[0].id);
    }
  }, [childrenList, selectedChildId]);

  useEffect(() => {
    if (selectedChildId) {
      localStorage.setItem('hero_selected_child_id', selectedChildId);
    }
  }, [selectedChildId]);

  useEffect(() => {
    localStorage.setItem('hero_parent_tasks', JSON.stringify(parentTasks));
  }, [parentTasks]);

  useEffect(() => {
    localStorage.setItem('hero_task_occurrences', JSON.stringify(taskOccurrences));
  }, [taskOccurrences]);
  
  // Day of year calculation for Today's Hero
  const now = new Date();
  const startOfYear = new Date(now.getFullYear(), 0, 0);
  const diff = now.getTime() - startOfYear.getTime();
  const oneDay = 1000 * 60 * 60 * 24;
  const dayOfYear = Math.floor(diff / oneDay);
  const defaultTodayHero = HEROES[(dayOfYear - 1) % HEROES.length] || HEROES[0];

  const [selectedHero, setSelectedHero] = useState<Hero>(defaultTodayHero);
  const isViewingToday = selectedHero.id === defaultTodayHero.id;

  // Favorites in localStorage
  const [favorites, setFavorites] = useState<string[]>(() => {
    try {
      const saved = localStorage.getItem('hero_favorites');
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  // Daily Deeds in localStorage
  const todayKey = `hero_deeds_${now.toISOString().split('T')[0]}`;
  const [completedDeedIds, setCompletedDeedIds] = useState<string[]>(() => {
    try {
      const saved = localStorage.getItem(todayKey);
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  // Honored Heroes in localStorage
  const [honoredHeroes, setHonoredHeroes] = useState<HonoredHero[]>(() => {
    try {
      const saved = localStorage.getItem('honored_heroes_list');
      return saved ? JSON.parse(saved) : [
        {
          id: '1',
          name: 'أمي الغالية',
          relation: 'والدتي الحبيبة',
          reason: 'تضحيتها المستمرة وسهرها على راحتنا دون كلل أو ملل، وقلبها الذي يفيض رحمة وأماناً.',
          badgeName: 'وسام الوفاء والعطاء',
          date: 'اليوم'
        }
      ];
    } catch {
      return [];
    }
  });

  // Quiz state
  const [quizOpen, setQuizOpen] = useState(false);
  const [currentQuizStep, setCurrentQuizStep] = useState(0);
  const [quizAnswers, setQuizAnswers] = useState<string[]>([]);
  const [quizResult, setQuizResult] = useState<any>(null);

  // Search & Filter in archive
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('all');

  // New honor form
  const [showHonorForm, setShowHonorForm] = useState(false);
  const [newHeroName, setNewHeroName] = useState('');
  const [newHeroRelation, setNewHeroRelation] = useState('والدتي الحبيبة');
  const [newHeroReason, setNewHeroReason] = useState('');
  const [newHeroBadge, setNewHeroBadge] = useState('وسام الوفاء والعطاء');

  // Copy/Share toast
  const [copiedNotification, setCopiedNotification] = useState('');

  useEffect(() => {
    localStorage.setItem('hero_favorites', JSON.stringify(favorites));
  }, [favorites]);

  useEffect(() => {
    localStorage.setItem(todayKey, JSON.stringify(completedDeedIds));
  }, [completedDeedIds, todayKey]);

  useEffect(() => {
    localStorage.setItem('honored_heroes_list', JSON.stringify(honoredHeroes));
  }, [honoredHeroes]);

  const toggleFavorite = (id: string) => {
    if (favorites.includes(id)) {
      setFavorites(favorites.filter(f => f !== id));
    } else {
      setFavorites([...favorites, id]);
      triggerSmallSparkle();
    }
  };

  const toggleDeed = (deedId: string) => {
    if (completedDeedIds.includes(deedId)) {
      setCompletedDeedIds(completedDeedIds.filter(id => id !== deedId));
    } else {
      const updated = [...completedDeedIds, deedId];
      setCompletedDeedIds(updated);
      if (updated.length === DEFAULT_DEEDS.length) {
        confetti({
          particleCount: 80,
          spread: 70,
          origin: { y: 0.6 }
        });
      }
    }
  };

  const triggerSmallSparkle = () => {
    confetti({
      particleCount: 25,
      spread: 45,
      origin: { y: 0.7 }
    });
  };

  const handleAddHonoredHero = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newHeroName.trim() || !newHeroReason.trim()) return;

    const newHero: HonoredHero = {
      id: Date.now().toString(),
      name: newHeroName.trim(),
      relation: newHeroRelation,
      reason: newHeroReason.trim(),
      badgeName: newHeroBadge,
      date: new Date().toLocaleDateString('ar-EG', { month: 'short', day: 'numeric' })
    };

    setHonoredHeroes([newHero, ...honoredHeroes]);
    setNewHeroName('');
    setNewHeroReason('');
    setShowHonorForm(false);
    confetti({
      particleCount: 60,
      spread: 60,
      origin: { y: 0.6 }
    });
  };

  const handleDeleteHonored = (id: string) => {
    setHonoredHeroes(honoredHeroes.filter(h => h.id !== id));
  };

  const handleShareQuote = (hero: Hero) => {
    const text = `🌟 بطل اليوم: ${hero.name}\n${hero.title}\n\n« ${hero.quote} »\n\nتطبيق بطل اليوم ✨`;
    if (navigator.share) {
      navigator.share({ title: 'بطل اليوم', text }).catch(() => {});
    } else {
      navigator.clipboard.writeText(text);
      showToast('تم نسخ اقتباس البطل للمشاركة!');
    }
  };

  const handleShareHonored = (hero: HonoredHero) => {
    const text = `🏆 شهادة تقدير بطل اليوم!\n\nيسعدني أن أمنح لقب بطل اليوم إلى:\n${hero.name} (${hero.relation})\n\n« ${hero.reason} »\nالوسام المستحق: ${hero.badgeName} 🎖️\n- تطبيق بطل اليوم`;
    if (navigator.share) {
      navigator.share({ title: 'شهادة بطل اليوم', text }).catch(() => {});
    } else {
      navigator.clipboard.writeText(text);
      showToast('تم نسخ وثيقة التكريم بنجاح!');
    }
  };

  const showToast = (msg: string) => {
    setCopiedNotification(msg);
    setTimeout(() => setCopiedNotification(''), 3000);
  };

  const handleQuizAnswer = (cat: string) => {
    const nextAnswers = [...quizAnswers, cat];
    setQuizAnswers(nextAnswers);

    if (currentQuizStep < QUIZ_QUESTIONS.length - 1) {
      setCurrentQuizStep(currentQuizStep + 1);
    } else {
      // Tally results
      const counts: Record<string, number> = {};
      nextAnswers.forEach(c => counts[c] = (counts[c] || 0) + 1);
      const topCat = Object.keys(counts).reduce((a, b) => counts[a] > counts[b] ? a : b, 'wisdom');
      const matchedHero = HEROES.find(h => h.category === topCat) || HEROES[0];

      const archetypes: Record<string, { title: string; desc: string }> = {
        science: {
          title: 'المبتكر والمستكشف الدقيق',
          desc: 'شخصيتك تبحث دوماً عن الحقيقة والبرهان، شغفك بالعلم يجعلك قادراً على ابتكار حلول عبقرية تسهل حياة الناس.'
        },
        wisdom: {
          title: 'الحكيم صاحب الرؤية والمعلم',
          desc: 'تزن الأمور برصانة وعمق، تضع اللبنات التي تدوم طويلاً، وتؤمن بأن بناء العقول هو أعظم انتصار.'
        },
        courage: {
          title: 'الفارس الجسور وحامي المستضعفين',
          desc: 'لا ترضى بالظلم ولا تهاب المواقف الصعبة، شجاعتك تلهم المحيطين وتمنحهم الأمان والثقة.'
        },
        humanity: {
          title: 'ينبوع الرحمة والملهم الإنساني',
          desc: 'بطولتك تتجلى في إحساسك العالي بالآخرين وعطائك النقي الذي يزرع الأمل في النفوس.'
        },
        everyday: {
          title: 'بطل الواقع والصانع الصامت',
          desc: 'أنت من الجنود المجهولين الذين يبنون العالم بتفانيهم اليومي الصادق دون انتظار أضواء الشهرة.'
        }
      };

      setQuizResult({
        category: topCat,
        matchedHero,
        ...archetypes[topCat]
      });
      confetti({ particleCount: 50, spread: 60 });
    }
  };

  const resetQuiz = () => {
    setCurrentQuizStep(0);
    setQuizAnswers([]);
    setQuizResult(null);
  };

  const getHeroIcon = (name: string) => {
    switch (name) {
      case 'Stethoscope': return <Stethoscope className="w-8 h-8 text-amber-500" />;
      case 'GraduationCap': return <GraduationCap className="w-8 h-8 text-amber-500" />;
      case 'Compass': return <Compass className="w-8 h-8 text-amber-500" />;
      case 'Binary': return <Binary className="w-8 h-8 text-amber-500" />;
      case 'Eye': return <Eye className="w-8 h-8 text-amber-500" />;
      case 'HeartHandshake': return <HeartHandshake className="w-8 h-8 text-amber-500" />;
      case 'ShieldAlert': return <ShieldAlert className="w-8 h-8 text-amber-500" />;
      case 'Heart': return <Heart className="w-8 h-8 text-amber-500" />;
      default: return <Star className="w-8 h-8 text-amber-500" />;
    }
  };

  const totalPoints = completedDeedIds.reduce((sum, id) => {
    const deed = DEFAULT_DEEDS.find(d => d.id === id);
    return sum + (deed ? deed.points : 0);
  }, 0);

  const filteredHeroes = HEROES.filter(hero => {
    const matchesSearch = searchQuery === '' ||
      hero.name.includes(searchQuery) ||
      hero.title.includes(searchQuery) ||
      hero.story.includes(searchQuery);
    const matchesCategory = selectedCategory === 'all' || hero.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-900 via-slate-900 to-slate-950 text-slate-100 flex flex-col font-sans">
      
      {/* Top App Bar */}
      <header className="sticky top-0 z-40 bg-slate-900/80 backdrop-blur-md border-b border-slate-800 px-4 py-3 sm:px-6">
        <div className="max-w-4xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-amber-400 to-amber-600 flex items-center justify-center shadow-lg shadow-amber-500/20">
              <Award className="w-6 h-6 text-slate-950" />
            </div>
            <div>
              <h1 className="text-xl font-black text-amber-400 tracking-wide">بطل اليوم</h1>
              <p className="text-xs text-slate-400">إلهام، بطولات يومية، وتكريم العطاء</p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setShowParentsModal(true)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-amber-500/10 hover:bg-amber-500/20 text-amber-400 text-xs font-bold transition border border-amber-500/30"
              title="قسم الوالدين"
            >
              <Lock className="w-3.5 h-3.5" />
              <span>للأهل</span>
            </button>

            <button
              onClick={() => {
                const otherHeroes = HEROES.filter(h => h.id !== selectedHero.id);
                const random = otherHeroes[Math.floor(Math.random() * otherHeroes.length)];
                setSelectedHero(random);
                setActiveTab('today');
                showToast(`تم الانتقال إلى: ${random.name}`);
              }}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold transition border border-slate-700"
              title="بطل عشوائي"
            >
              <Shuffle className="w-3.5 h-3.5 text-amber-400" />
              <span>إلهام آخر</span>
            </button>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-4xl w-full mx-auto p-4 sm:p-6 pb-24">
        
        {/* TAB 1: TODAY'S HERO */}
        {activeTab === 'today' && (
          <div className="space-y-6 animate-fadeIn">
            {/* Status Banner */}
            <div className="flex items-center justify-between bg-slate-800/60 border border-slate-700/60 rounded-2xl p-4">
              <div className="flex items-center gap-2.5">
                <span className="flex h-3 w-3 relative">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-3 w-3 bg-amber-500"></span>
                </span>
                <span className="text-sm font-bold text-amber-400">
                  {isViewingToday ? 'بطل هذا اليوم الرسمي' : 'استكشاف سيرة بطل'}
                </span>
              </div>

              <div className="flex items-center gap-2">
                {!isViewingToday && (
                  <button
                    onClick={() => setSelectedHero(defaultTodayHero)}
                    className="text-xs text-slate-300 hover:text-amber-400 font-medium underline transition"
                  >
                    العودة لبطل اليوم
                  </button>
                )}
                <button
                  onClick={() => toggleFavorite(selectedHero.id)}
                  className={`p-2 rounded-xl transition ${
                    favorites.includes(selectedHero.id) 
                      ? 'bg-amber-500/20 text-amber-400 border border-amber-500/40' 
                      : 'bg-slate-800 text-slate-400 hover:text-slate-200'
                  }`}
                  title="حفظ في المفضلة"
                >
                  {favorites.includes(selectedHero.id) ? (
                    <BookmarkCheck className="w-5 h-5 text-amber-400" />
                  ) : (
                    <Bookmark className="w-5 h-5" />
                  )}
                </button>
                <button
                  onClick={() => handleShareQuote(selectedHero)}
                  className="p-2 rounded-xl bg-slate-800 text-slate-400 hover:text-slate-200 transition"
                  title="مشاركة"
                >
                  <Share2 className="w-5 h-5" />
                </button>
              </div>
            </div>

            {/* Hero Main Showcase Card */}
            <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-slate-800/90 via-slate-800/60 to-slate-900 border border-amber-500/30 p-6 sm:p-8 shadow-2xl shadow-amber-500/5">
              <div className="absolute -top-12 -left-12 w-48 h-48 bg-amber-500/10 rounded-full blur-3xl pointer-events-none"></div>

              <div className="flex flex-col sm:flex-row items-center sm:items-start gap-6">
                {/* Hero Avatar Badge */}
                <div className="relative group">
                  <div className="w-24 h-24 sm:w-28 sm:h-28 rounded-2xl bg-gradient-to-tr from-slate-900 via-slate-800 to-slate-700 border-2 border-amber-400/80 flex items-center justify-center shadow-xl shadow-amber-500/20">
                    {getHeroIcon(selectedHero.iconName)}
                  </div>
                  <div className="absolute -bottom-2 -right-2 bg-amber-500 text-slate-950 text-[10px] font-black px-2 py-0.5 rounded-full shadow">
                    {selectedHero.categoryArabic}
                  </div>
                </div>

                {/* Hero Info */}
                <div className="flex-1 text-center sm:text-right space-y-1.5">
                  <h2 className="text-2xl sm:text-3xl font-black text-slate-100">{selectedHero.name}</h2>
                  <p className="text-base font-semibold text-amber-400">{selectedHero.title}</p>
                  <p className="text-xs text-slate-400">📍 {selectedHero.era}</p>

                  {/* Virtues Tags */}
                  <div className="flex flex-wrap gap-2 pt-3 justify-center sm:justify-start">
                    {selectedHero.virtues.map((v, i) => (
                      <span key={i} className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-amber-400/10 border border-amber-400/20 text-amber-300 text-xs font-semibold">
                        <Sparkles className="w-3 h-3 text-amber-400" />
                        {v}
                      </span>
                    ))}
                  </div>
                </div>
              </div>

              {/* Quote Card */}
              <div className="mt-6 p-4 sm:p-5 rounded-2xl bg-slate-900/80 border border-slate-700/80 relative">
                <Quote className="w-6 h-6 text-amber-400/60 mb-2 rotate-180" />
                <p className="text-slate-200 text-base sm:text-lg leading-relaxed font-medium italic">
                  « {selectedHero.quote} »
                </p>
              </div>

              {/* Story */}
              <div className="mt-6 space-y-2">
                <h3 className="text-sm font-bold text-amber-400 flex items-center gap-2">
                  <BookOpen className="w-4 h-4" />
                  قصة البطولة والأثر الإنساني
                </h3>
                <p className="text-slate-300 text-sm sm:text-base leading-relaxed bg-slate-900/40 p-4 rounded-2xl border border-slate-800">
                  {selectedHero.story}
                </p>
              </div>

              {/* Daily Lesson */}
              <div className="mt-6 p-4 sm:p-5 rounded-2xl bg-amber-500/10 border border-amber-500/30 flex items-start gap-3">
                <div className="p-2 rounded-xl bg-amber-400/20 text-amber-400 shrink-0 mt-0.5">
                  <Sparkles className="w-5 h-5" />
                </div>
                <div>
                  <h4 className="text-sm font-bold text-amber-300 mb-1">
                    كيف نقتبس من بطولته اليوم؟
                  </h4>
                  <p className="text-slate-300 text-xs sm:text-sm leading-relaxed">
                    {selectedHero.dailyHeroicLesson}
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: DAILY CHALLENGE / MY HEROIC DEEDS */}
        {activeTab === 'challenge' && (
          <div className="space-y-6 animate-fadeIn">
            {/* Header & Stats */}
            <div className="bg-gradient-to-br from-slate-800 to-slate-900 rounded-3xl border border-slate-700/80 p-6 shadow-xl">
              <div className="text-center space-y-2 mb-6">
                <h2 className="text-2xl font-black text-slate-100">تحدي بطولتي اليومي</h2>
                <p className="text-slate-400 text-sm">البطولة تبدأ من عمل خير صغير تفعله بصدق كل يوم</p>
              </div>

              {/* Stats Bar */}
              <div className="grid grid-cols-2 gap-4 bg-slate-900/80 p-4 rounded-2xl border border-slate-800 mb-4">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-amber-500/20 flex items-center justify-center text-amber-400">
                    <Star className="w-5 h-5" />
                  </div>
                  <div>
                    <div className="text-lg font-black text-slate-100">{totalPoints} نقطة</div>
                    <div className="text-xs text-slate-400">نقاط بطولة اليوم</div>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-rose-500/20 flex items-center justify-center text-rose-400">
                    <Flame className="w-5 h-5" />
                  </div>
                  <div>
                    <div className="text-lg font-black text-slate-100">{completedDeedIds.length} من {DEFAULT_DEEDS.length}</div>
                    <div className="text-xs text-slate-400">المهام المنجزة</div>
                  </div>
                </div>
              </div>

              {/* Progress bar */}
              <div className="w-full bg-slate-800 rounded-full h-2.5 overflow-hidden">
                <div 
                  className="bg-gradient-to-r from-amber-500 to-amber-300 h-2.5 rounded-full transition-all duration-500"
                  style={{ width: `${(completedDeedIds.length / DEFAULT_DEEDS.length) * 100}%` }}
                ></div>
              </div>

              {/* Celebratory badge if 100% */}
              {completedDeedIds.length === DEFAULT_DEEDS.length && (
                <div className="mt-5 p-4 rounded-2xl bg-gradient-to-r from-emerald-950/80 to-emerald-900/50 border border-emerald-500/40 flex items-center gap-3 animate-fadeIn">
                  <Trophy className="w-8 h-8 text-amber-400 shrink-0" />
                  <div>
                    <div className="text-sm font-black text-emerald-300">مبارك! نلت وسام بطل اليوم المتوج 👑</div>
                    <div className="text-xs text-emerald-200/80">أتممت جميع أعمال البطولة اليومية. أثرك يصنع فارقاً حقيقياً!</div>
                  </div>
                </div>
              )}
            </div>

            {/* Effort praise notification banner */}
            {effortPraise && (
              <div className="p-4 rounded-2xl bg-amber-500/15 border border-amber-500/30 flex items-center justify-between text-amber-300 animate-fadeIn">
                <div className="flex items-center gap-2">
                  <Sparkles className="w-5 h-5 text-amber-400 shrink-0" />
                  <span className="text-xs sm:text-sm font-bold">{effortPraise}</span>
                </div>
                <button onClick={() => setEffortPraise(null)} className="text-xs text-amber-400 hover:text-amber-200">
                  إغلاق
                </button>
              </div>
            )}

            {/* Child Selector if children exist */}
            {childrenList.filter(c => !c.isArchived).length > 0 && (
              <div className="p-4 rounded-2xl bg-slate-800/80 border border-slate-700/80 space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-slate-300 flex items-center gap-2">
                    <User className="w-4 h-4 text-amber-400" />
                    اختيار البطل لمتابعة مهامه المخصصة:
                  </span>
                  <button 
                    onClick={() => setShowParentsModal(true)}
                    className="text-xs text-amber-400 hover:underline font-medium"
                  >
                    إدارة من قسم الأهل
                  </button>
                </div>
                <div className="flex items-center gap-2 overflow-x-auto pb-1">
                  {childrenList.filter(c => !c.isArchived).map(child => {
                    const isSelected = child.id === selectedChildId;
                    return (
                      <button
                        key={child.id}
                        onClick={() => setSelectedChildId(child.id)}
                        className={`px-3 py-1.5 rounded-xl text-xs font-bold transition flex items-center gap-1.5 whitespace-nowrap ${
                          isSelected 
                            ? 'bg-amber-500 text-slate-950 shadow-md shadow-amber-500/20' 
                            : 'bg-slate-700/60 text-slate-300 hover:bg-slate-700'
                        }`}
                      >
                        <span>★</span>
                        <span>{child.alias}</span>
                      </button>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Parent Assigned Tasks for Selected Child */}
            {selectedChildId && (
              (() => {
                const todayStr = now.toISOString().split('T')[0];
                const activeOccs = taskOccurrences.filter(o => o.childId === selectedChildId && o.dateStr === todayStr);
                const selectedChild = childrenList.find(c => c.id === selectedChildId);
                if (activeOccs.length === 0) return null;
                return (
                  <div className="space-y-3">
                    <h3 className="text-sm font-bold text-amber-400 flex items-center gap-2 px-1">
                      <Sparkles className="w-4 h-4" />
                      مهام الوالدين المخصصة لـ {selectedChild?.alias || 'البطل'}:
                    </h3>
                    {activeOccs.map(occ => {
                      return (
                        <div key={occ.id} className="p-4 rounded-2xl bg-slate-800/90 border border-amber-500/30 space-y-3">
                          <div className="flex items-start justify-between gap-2">
                            <div>
                              <h4 className="font-bold text-sm text-slate-100">{occ.snapshotTitle}</h4>
                              {occ.snapshotDescription && (
                                <p className="text-xs text-slate-400 mt-0.5">{occ.snapshotDescription}</p>
                              )}
                            </div>
                            <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-amber-500/10 text-amber-300 border border-amber-500/20 shrink-0">
                              {occ.status === 'COMPLETED' ? 'مكتملة ✓' : occ.status === 'PENDING_APPROVAL' ? 'بانتظار التأكيد' : occ.status === 'SKIPPED' ? 'تم تخطيها' : 'مطلوبة'}
                            </span>
                          </div>

                          {occ.parentFeedbackNote && (
                            <div className="p-2.5 rounded-xl bg-amber-500/10 border border-amber-500/20 text-xs text-amber-300">
                              💬 ملاحظة الوالدين: {occ.parentFeedbackNote}
                            </div>
                          )}

                          {occ.status === 'NOT_STARTED' && (
                            <div className="flex items-center gap-2 pt-1">
                              <button
                                onClick={() => {
                                  if (occ.requiresApproval) {
                                    setTaskOccurrences(prev => prev.map(o => o.id === occ.id ? { ...o, status: 'PENDING_APPROVAL' } : o));
                                  } else {
                                    setTaskOccurrences(prev => prev.map(o => o.id === occ.id ? { ...o, status: 'COMPLETED' } : o));
                                    setEffortPraise('محاولة رائعة! كل خطوة تصنع منك بطلاً حقيقيًا!');
                                    confetti({ particleCount: 35, spread: 50, origin: { y: 0.6 } });
                                  }
                                }}
                                className="flex-1 py-2 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold text-xs flex items-center justify-center gap-1.5 transition"
                              >
                                <Check className="w-4 h-4 stroke-[3]" />
                                <span>أنجزتها!</span>
                              </button>
                              <button
                                onClick={() => {
                                  setTaskOccurrences(prev => prev.map(o => o.id === occ.id ? { ...o, status: 'SKIPPED' } : o));
                                }}
                                className="px-3 py-2 rounded-xl bg-slate-700 hover:bg-slate-650 text-slate-300 font-bold text-xs transition"
                              >
                                هعدّيها النهارده
                              </button>
                            </div>
                          )}

                          {occ.status === 'PENDING_APPROVAL' && (
                            <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-900 border border-amber-500/20">
                              <span className="text-xs text-amber-300 flex items-center gap-1.5 font-medium">
                                <Hourglass className="w-4 h-4 text-amber-400" />
                                بانتظار مراجعة وتأكيد ولي الأمر...
                              </span>
                              <button
                                onClick={() => {
                                  setTaskOccurrences(prev => prev.map(o => o.id === occ.id ? { ...o, status: 'NOT_STARTED' } : o));
                                }}
                                className="text-xs text-slate-400 hover:text-slate-200 underline"
                              >
                                تراجع
                              </button>
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                );
              })()
            )}

            {/* Checklist */}
            <div className="space-y-3">
              <h3 className="text-sm font-bold text-slate-300 px-1">مهام اليوم:</h3>
              {DEFAULT_DEEDS.map(deed => {
                const isDone = completedDeedIds.includes(deed.id);
                return (
                  <div
                    key={deed.id}
                    onClick={() => toggleDeed(deed.id)}
                    className={`cursor-pointer transition-all duration-200 p-4 sm:p-5 rounded-2xl border flex items-start gap-4 ${
                      isDone 
                        ? 'bg-slate-900/60 border-emerald-500/40 text-slate-300' 
                        : 'bg-slate-800/70 border-slate-700/80 hover:border-amber-500/40 text-slate-100'
                    }`}
                  >
                    <div className={`mt-0.5 w-6 h-6 rounded-full flex items-center justify-center transition ${
                      isDone ? 'bg-emerald-500 text-slate-950' : 'border-2 border-slate-600'
                    }`}>
                      {isDone && <Check className="w-4 h-4 stroke-[3]" />}
                    </div>

                    <div className="flex-1">
                      <div className="flex items-center justify-between gap-2">
                        <span className={`font-bold text-base ${isDone ? 'line-through text-slate-400' : 'text-slate-100'}`}>
                          {deed.title}
                        </span>
                        <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-400">
                          +{deed.points} نقطة
                        </span>
                      </div>
                      <p className="text-xs sm:text-sm text-slate-400 mt-1 leading-relaxed">
                        {deed.description}
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* TAB 3: HONOR A HERO */}
        {activeTab === 'honor' && (
          <div className="space-y-6 animate-fadeIn">
            {/* Header */}
            <div className="flex flex-col sm:flex-row items-center justify-between gap-4 bg-slate-800/80 p-6 rounded-3xl border border-slate-700/80">
              <div className="text-center sm:text-right space-y-1">
                <h2 className="text-2xl font-black text-slate-100">لوحة شرف أبطال حياتك</h2>
                <p className="text-slate-400 text-sm">من هو الشخص الذي تعتبره بطلاً في حياتك وتود تكريمه اليوم؟</p>
              </div>
              <button
                onClick={() => setShowHonorForm(!showHonorForm)}
                className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-bold text-sm shadow-lg shadow-amber-500/20 transition"
              >
                <PlusCircle className="w-4 h-4" />
                <span>{showHonorForm ? 'إلغاء' : 'منح وسام بطل'}</span>
              </button>
            </div>

            {/* Honor Form */}
            {showHonorForm && (
              <form onSubmit={handleAddHonoredHero} className="bg-slate-800/90 border border-amber-500/40 rounded-3xl p-6 space-y-4 animate-fadeIn">
                <h3 className="text-base font-bold text-amber-400">إصدار وثيقة تقدير وبطولة جديدة:</h3>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">اسم بطلك العزيز:</label>
                  <input
                    type="text"
                    required
                    placeholder="مثال: أمي فاطمة، أستاذ محمود، صديقي خالد..."
                    value={newHeroName}
                    onChange={e => setNewHeroName(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-slate-100 text-sm focus:border-amber-400 outline-none"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">صلة القرابة أو العلاقة:</label>
                  <div className="flex flex-wrap gap-2">
                    {['والدتي الحبيبة', 'والدي الكريم', 'معلمي الفاضل', 'رفيق دربي', 'طبيبي المخلص', 'أختي السند', 'ابني الغالي'].map(rel => (
                      <button
                        type="button"
                        key={rel}
                        onClick={() => setNewHeroRelation(rel)}
                        className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
                          newHeroRelation === rel 
                            ? 'bg-amber-500 text-slate-950' 
                            : 'bg-slate-900 text-slate-300 hover:bg-slate-700'
                        }`}
                      >
                        {rel}
                      </button>
                    ))}
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">لماذا يستحق لقب بطل اليوم؟</label>
                  <textarea
                    required
                    rows={3}
                    placeholder="اكتب كلمة شكر وسبب التقدير أو موقفاً نبيلاً لا تنساه له..."
                    value={newHeroReason}
                    onChange={e => setNewHeroReason(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-slate-100 text-sm focus:border-amber-400 outline-none"
                  ></textarea>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">الوسام الممنوح:</label>
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                    {[
                      { name: 'وسام الوفاء والعطاء', color: 'text-amber-400' },
                      { name: 'وسام الشجاعة والصمود', color: 'text-rose-400' },
                      { name: 'وسام الصبر العظيم', color: 'text-purple-400' },
                      { name: 'وسام النور والمعرفة', color: 'text-blue-400' }
                    ].map(b => (
                      <button
                        type="button"
                        key={b.name}
                        onClick={() => setNewHeroBadge(b.name)}
                        className={`p-2.5 rounded-xl text-xs font-bold border flex flex-col items-center gap-1 transition ${
                          newHeroBadge === b.name 
                            ? 'bg-amber-500/20 border-amber-400 text-amber-300' 
                            : 'bg-slate-900 border-slate-700 text-slate-400 hover:border-slate-600'
                        }`}
                      >
                        <Award className="w-4 h-4" />
                        <span>{b.name}</span>
                      </button>
                    ))}
                  </div>
                </div>

                <div className="pt-2">
                  <button
                    type="submit"
                    className="w-full py-3 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-black text-sm shadow-lg transition flex items-center justify-center gap-2"
                  >
                    <Trophy className="w-4 h-4" />
                    <span>إصدار وثيقة التكريم</span>
                  </button>
                </div>
              </form>
            )}

            {/* List of Honored Heroes */}
            <div className="space-y-4">
              {honoredHeroes.length === 0 ? (
                <div className="text-center py-12 bg-slate-800/40 rounded-3xl border border-slate-800 p-6">
                  <HeartHandshake className="w-12 h-12 text-slate-600 mx-auto mb-3" />
                  <p className="text-slate-400 text-sm">لم تقم بإضافة أي بطل بعد. ابدأ بتكريم شخص أضاء حياتك!</p>
                </div>
              ) : (
                honoredHeroes.map(hero => (
                  <div 
                    key={hero.id}
                    className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-slate-800/90 to-slate-900 border border-amber-500/30 p-6 shadow-xl"
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <div className="flex items-center gap-2">
                          <h3 className="text-xl font-black text-amber-400">{hero.name}</h3>
                          <span className="text-xs px-2.5 py-0.5 rounded-full bg-slate-700 text-slate-300 font-medium">
                            {hero.relation}
                          </span>
                        </div>
                        <div className="inline-flex items-center gap-1.5 mt-2 px-3 py-1 rounded-full bg-amber-400/10 border border-amber-400/20 text-amber-300 text-xs font-bold">
                          <Award className="w-3.5 h-3.5" />
                          <span>{hero.badgeName}</span>
                        </div>
                      </div>

                      <div className="flex items-center gap-1">
                        <button
                          onClick={() => handleShareHonored(hero)}
                          className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 transition"
                          title="مشاركة وثيقة التكريم"
                        >
                          <Share2 className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDeleteHonored(hero.id)}
                          className="p-2 rounded-xl bg-slate-800 hover:bg-rose-950 text-slate-400 hover:text-rose-400 transition"
                          title="حذف"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </div>

                    <div className="mt-4 p-4 rounded-2xl bg-slate-900/60 border border-slate-800">
                      <p className="text-slate-200 text-sm leading-relaxed italic">
                        « {hero.reason} »
                      </p>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        )}

        {/* TAB 4: HERO ARCHIVE & QUIZ */}
        {activeTab === 'archive' && (
          <div className="space-y-6 animate-fadeIn">
            {/* Banner with Quiz Trigger */}
            <div className="bg-gradient-to-r from-amber-600 via-amber-500 to-amber-600 rounded-3xl p-6 text-slate-950 shadow-xl flex flex-col sm:flex-row items-center justify-between gap-4">
              <div className="text-center sm:text-right">
                <h2 className="text-xl sm:text-2xl font-black">اختبار: من يشبهك من الأبطال؟</h2>
                <p className="text-xs sm:text-sm text-slate-900 font-medium mt-1">أجب عن 3 أسئلة لتكتشف نمط بطولتك الداخلي والبطل الأقرب لروحك</p>
              </div>
              <button
                onClick={() => {
                  resetQuiz();
                  setQuizOpen(true);
                }}
                className="px-6 py-2.5 rounded-xl bg-slate-950 text-amber-400 hover:bg-slate-900 font-black text-sm shadow-md transition shrink-0"
              >
                بدء الاختبار الآن
              </button>
            </div>

            {/* Search & Categories Filter */}
            <div className="space-y-3">
              <div className="relative">
                <Search className="w-4 h-4 text-slate-400 absolute right-4 top-3.5" />
                <input
                  type="text"
                  placeholder="ابحث بالاسم، الإنجاز، أو الكلمات المفتاحية..."
                  value={searchQuery}
                  onChange={e => setSearchQuery(e.target.value)}
                  className="w-full pr-11 pl-4 py-3 rounded-2xl bg-slate-800 border border-slate-700 text-slate-100 text-sm focus:border-amber-400 outline-none"
                />
              </div>

              <div className="flex flex-wrap gap-2">
                {[
                  { id: 'all', label: `الكل (${HEROES.length})` },
                  { id: 'science', label: 'علوم واكتشاف' },
                  { id: 'wisdom', label: 'فكر وحكمة' },
                  { id: 'courage', label: 'شجاعة وريادة' },
                  { id: 'humanity', label: 'إنسانية وعطاء' },
                  { id: 'everyday', label: 'أبطال واقعنا' }
                ].map(cat => (
                  <button
                    key={cat.id}
                    onClick={() => setSelectedCategory(cat.id)}
                    className={`px-3 py-1.5 rounded-xl text-xs font-bold transition ${
                      selectedCategory === cat.id 
                        ? 'bg-amber-500 text-slate-950' 
                        : 'bg-slate-800 text-slate-400 hover:text-slate-200'
                    }`}
                  >
                    {cat.label}
                  </button>
                ))}
              </div>
            </div>

            {/* Heroes Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {filteredHeroes.map(hero => (
                <div
                  key={hero.id}
                  onClick={() => {
                    setSelectedHero(hero);
                    setActiveTab('today');
                  }}
                  className="cursor-pointer group bg-slate-800/80 hover:bg-slate-800 rounded-3xl border border-slate-700/80 hover:border-amber-500/50 p-5 transition shadow-lg flex flex-col justify-between"
                >
                  <div>
                    <div className="flex items-start justify-between gap-3 mb-3">
                      <div className="w-12 h-12 rounded-2xl bg-slate-900 border border-amber-500/30 flex items-center justify-center group-hover:scale-105 transition">
                        {getHeroIcon(hero.iconName)}
                      </div>
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-700 text-slate-300">
                        {hero.categoryArabic}
                      </span>
                    </div>

                    <h3 className="text-lg font-black text-slate-100 group-hover:text-amber-400 transition">{hero.name}</h3>
                    <p className="text-xs font-semibold text-amber-400/90 mb-2">{hero.title}</p>
                    <p className="text-xs text-slate-400 line-clamp-2 leading-relaxed">
                      {hero.story}
                    </p>
                  </div>

                  <div className="mt-4 pt-3 border-t border-slate-700/60 flex items-center justify-between text-xs text-slate-400">
                    <span>{hero.era}</span>
                    <span className="text-amber-400 font-bold group-hover:translate-x-[-2px] transition">
                      عرض السيرة 👈
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </main>

      {/* QUIZ MODAL */}
      {quizOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-amber-500/40 rounded-3xl max-w-lg w-full p-6 sm:p-8 shadow-2xl relative animate-fadeIn">
            {quizResult ? (
              <div className="text-center space-y-4">
                <div className="w-16 h-16 rounded-2xl bg-amber-500/20 border border-amber-500/40 flex items-center justify-center mx-auto text-amber-400">
                  <Trophy className="w-8 h-8" />
                </div>
                <div className="text-xs font-bold text-amber-400">نمط بطولتك الداخلي:</div>
                <h3 className="text-2xl font-black text-slate-100">{quizResult.title}</h3>
                <p className="text-sm text-slate-300 leading-relaxed bg-slate-800/60 p-4 rounded-2xl border border-slate-700">
                  {quizResult.desc}
                </p>

                <div 
                  onClick={() => {
                    setSelectedHero(quizResult.matchedHero);
                    setQuizOpen(false);
                    setActiveTab('today');
                  }}
                  className="p-4 rounded-2xl bg-amber-500/10 border border-amber-500/30 hover:bg-amber-500/20 cursor-pointer transition flex items-center gap-3 text-right"
                >
                  <div className="w-12 h-12 rounded-xl bg-slate-900 flex items-center justify-center">
                    {getHeroIcon(quizResult.matchedHero.iconName)}
                  </div>
                  <div className="flex-1">
                    <div className="text-xs text-amber-400">البطل الأقرب لروحك:</div>
                    <div className="text-base font-bold text-slate-100">{quizResult.matchedHero.name}</div>
                  </div>
                  <ChevronLeft className="w-5 h-5 text-amber-400" />
                </div>

                <div className="flex gap-2 pt-2">
                  <button
                    onClick={resetQuiz}
                    className="flex-1 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-bold transition flex items-center justify-center gap-1.5"
                  >
                    <RotateCcw className="w-3.5 h-3.5" />
                    <span>إعادة الاختبار</span>
                  </button>
                  <button
                    onClick={() => setQuizOpen(false)}
                    className="flex-1 py-2.5 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 text-xs font-bold transition"
                  >
                    إغلاق
                  </button>
                </div>
              </div>
            ) : (
              <div className="space-y-5">
                <div className="flex items-center justify-between text-xs text-slate-400">
                  <span className="font-bold text-amber-400">سؤال {currentQuizStep + 1} من {QUIZ_QUESTIONS.length}</span>
                  <button onClick={() => setQuizOpen(false)} className="hover:text-slate-200">إغلاق ✕</button>
                </div>

                <h3 className="text-lg sm:text-xl font-bold text-slate-100 text-center leading-relaxed">
                  {QUIZ_QUESTIONS[currentQuizStep].question}
                </h3>

                <div className="space-y-2.5 pt-2">
                  {QUIZ_QUESTIONS[currentQuizStep].options.map((opt, idx) => (
                    <button
                      key={idx}
                      onClick={() => handleQuizAnswer(opt.category)}
                      className="w-full text-right p-4 rounded-2xl bg-slate-800 hover:bg-slate-750 border border-slate-700 hover:border-amber-500/50 text-slate-200 hover:text-slate-100 text-sm font-medium transition flex items-center gap-3"
                    >
                      <span className="w-6 h-6 rounded-full bg-slate-900 border border-slate-700 flex items-center justify-center text-xs font-bold text-amber-400 shrink-0">
                        {idx + 1}
                      </span>
                      <span>{opt.text}</span>
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Toast Notification */}
      {copiedNotification && (
        <div className="fixed bottom-20 left-1/2 -translate-x-1/2 z-50 bg-amber-500 text-slate-950 px-4 py-2 rounded-full font-bold text-xs shadow-xl animate-fadeIn">
          {copiedNotification}
        </div>
      )}

      {/* Parents Section Modal */}
      <ParentsSectionModal
        isOpen={showParentsModal}
        onClose={() => setShowParentsModal(false)}
        childrenList={childrenList}
        setChildrenList={setChildrenList}
        parentTasks={parentTasks}
        setParentTasks={setParentTasks}
        occurrences={taskOccurrences}
        setOccurrences={setTaskOccurrences}
        onPraise={(msg) => setEffortPraise(msg)}
      />

      {/* Bottom Navigation Bar */}
      <nav className="fixed bottom-0 left-0 right-0 z-40 bg-slate-900/95 backdrop-blur-lg border-t border-slate-800 px-4 py-2">
        <div className="max-w-md mx-auto grid grid-cols-4 gap-1">
          {[
            { id: 'today', label: 'بطل اليوم', icon: Award },
            { id: 'challenge', label: 'تحدي بطولتي', icon: CheckCircle2 },
            { id: 'honor', label: 'كرّم بطلك', icon: HeartHandshake },
            { id: 'archive', label: 'الأرشيف', icon: BookOpen }
          ].map(tab => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                className={`flex flex-col items-center justify-center py-1.5 px-2 rounded-2xl transition ${
                  isActive 
                    ? 'text-amber-400 bg-amber-500/10' 
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                <Icon className={`w-5 h-5 mb-1 ${isActive ? 'stroke-[2.5]' : 'stroke-2'}`} />
                <span className="text-[11px] font-bold">{tab.label}</span>
              </button>
            );
          })}
        </div>
      </nav>
    </div>
  );
}
