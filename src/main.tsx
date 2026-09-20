import React, { Component, ErrorInfo, ReactNode } from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import './index.css'

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error("Uncaught error:", error, errorInfo);
  }

  public render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex flex-col items-center justify-center p-6 text-center bg-slate-50 text-slate-800" dir="rtl">
          <div className="w-16 h-16 bg-amber-100 rounded-full flex items-center justify-center mb-4 text-amber-600 text-2xl">
            ⚠️
          </div>
          <h1 className="text-xl font-bold mb-2">عذراً، حدث خطأ غير متوقع</h1>
          <p className="text-slate-600 text-sm mb-6 max-w-md">
            نعتذر عن هذا الخطأ، يمكنك محاولة إعادة تشغيل التطبيق بالضغط على الزر أدناه.
          </p>
          <button
            onClick={() => window.location.reload()}
            className="px-6 py-2.5 bg-amber-500 hover:bg-amber-600 text-white font-medium rounded-xl shadow transition"
          >
            إعادة التحميل
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ErrorBoundary>
      <App />
    </ErrorBoundary>
  </React.StrictMode>,
)

