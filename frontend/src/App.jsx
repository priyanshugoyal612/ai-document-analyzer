import { useState } from 'react'
import { FileText, MessageSquare, Upload, Settings, Eye, Sparkles } from 'lucide-react'
import DocumentUpload from './components/DocumentUpload'
import DocumentList from './components/DocumentList'
import ChatInterface from './components/ChatInterface'
import DirectChat from './components/DirectChat'
import VisionAnalysis from './components/VisionAnalysis'

function App() {
  const [activeTab, setActiveTab] = useState('upload')

  const tabs = [
    { id: 'upload', label: 'Upload', icon: Upload },
    { id: 'documents', label: 'Documents', icon: FileText },
    { id: 'chat', label: 'Chat', icon: MessageSquare },
    { id: 'direct', label: 'Direct Chat', icon: Settings },
    { id: 'vision', label: 'Vision', icon: Eye },
  ]

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50">
      {/* Header */}
      <header className="bg-white/80 backdrop-blur-lg border-b border-white/20 shadow-sm sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-5">
            <div className="flex items-center space-x-3">
              <div className="bg-gradient-to-br from-indigo-500 to-purple-600 p-2.5 rounded-xl shadow-lg">
                <Sparkles className="h-6 w-6 text-white" />
              </div>
              <div>
                <h1 className="text-2xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
                  AI Document Analyzer
                </h1>
                <p className="text-xs text-gray-500 font-medium">Powered by GPT-4o</p>
              </div>
            </div>
            <div className="flex items-center space-x-2">
              <span className="px-4 py-2 bg-gradient-to-r from-indigo-100 to-purple-100 text-indigo-700 rounded-full text-sm font-semibold shadow-sm">
                PageIndex + OpenAI
              </span>
            </div>
          </div>
        </div>
      </header>

      {/* Navigation */}
      <nav className="bg-white/60 backdrop-blur-md border-b border-white/20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex space-x-1">
            {tabs.map((tab) => {
              const Icon = tab.icon
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`flex items-center space-x-2 px-5 py-4 rounded-t-lg font-medium text-sm transition-all duration-200 ${
                    activeTab === tab.id
                      ? 'bg-white text-indigo-600 shadow-md border-t-2 border-indigo-500'
                      : 'text-gray-600 hover:text-indigo-600 hover:bg-white/50'
                  }`}
                >
                  <Icon className="h-5 w-5" />
                  <span>{tab.label}</span>
                </button>
              )
            })}
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {activeTab === 'upload' && <DocumentUpload />}
        {activeTab === 'documents' && <DocumentList />}
        {activeTab === 'chat' && <ChatInterface />}
        {activeTab === 'direct' && <DirectChat />}
        {activeTab === 'vision' && <VisionAnalysis />}
      </main>
    </div>
  )
}

export default App
