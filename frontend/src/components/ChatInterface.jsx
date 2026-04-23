import { useState, useEffect } from 'react'
import { Send, MessageSquare, Loader2, FileText } from 'lucide-react'

export default function ChatInterface() {
  const [documents, setDocuments] = useState([])
  const [selectedDocument, setSelectedDocument] = useState(null)
  const [chatType, setChatType] = useState('openai') // 'pageindex' or 'openai'
  const [query, setQuery] = useState('')
  const [chatHistory, setChatHistory] = useState([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    fetchDocuments()
  }, [])

  const fetchDocuments = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/documents')
      const data = await response.json()
      console.log('Fetched documents:', data)
      const completedDocs = data.filter(doc => doc.status === 'COMPLETED')
      console.log('Completed documents:', completedDocs)
      setDocuments(completedDocs)
      if (completedDocs.length > 0) {
        setSelectedDocument(String(completedDocs[0].id))
      }
    } catch (error) {
      console.error('Error fetching documents:', error)
    }
  }

  const handleSendMessage = async () => {
    if (!query.trim()) return

    setLoading(true)
    const userMessage = { role: 'user', content: query }
    setChatHistory([...chatHistory, userMessage])

    try {
      let endpoint
      if (selectedDocument) {
        endpoint = chatType === 'pageindex'
          ? `http://localhost:8080/api/query/document/${selectedDocument}`
          : `http://localhost:8080/api/openai/chat/document/${selectedDocument}`
      } else {
        endpoint = chatType === 'pageindex'
          ? 'http://localhost:8080/api/query'
          : 'http://localhost:8080/api/openai/chat'
      }

      const response = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query })
      })

      const data = await response.json()
      const assistantMessage = { role: 'assistant', content: data.answer || data.message || 'No response' }
      setChatHistory([...chatHistory, userMessage, assistantMessage])
    } catch (error) {
      const errorMessage = { role: 'assistant', content: 'Error: Failed to get response' }
      setChatHistory([...chatHistory, userMessage, errorMessage])
    } finally {
      setLoading(false)
      setQuery('')
    }
  }

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSendMessage()
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
          Chat with Documents
        </h2>
        <p className="mt-2 text-gray-600">Ask questions about your documents using PageIndex or OpenAI</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        {/* Sidebar */}
        <div className="lg:col-span-1 space-y-4">
          <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl p-6 space-y-5 border border-white/20">
            <h3 className="font-semibold text-gray-900">Settings</h3>
            
            {/* Chat Type Selection */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Chat Method
              </label>
              <select
                value={chatType}
                onChange={(e) => setChatType(e.target.value)}
                className="w-full px-4 py-2.5 border border-gray-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white/50 transition-all duration-200"
              >
                <option value="openai">OpenAI</option>
                <option value="pageindex">PageIndex</option>
              </select>
            </div>

            {/* Document Selection */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Document
              </label>
              <select
                value={selectedDocument || ''}
                onChange={(e) => {
                  console.log('Selected document:', e.target.value)
                  setSelectedDocument(e.target.value)
                }}
                className="w-full px-4 py-2.5 border border-gray-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white/50 transition-all duration-200"
              >
                <option value="">All Documents</option>
                {documents.map((doc) => (
                  <option key={doc.id} value={String(doc.id)}>
                    {doc.filename}
                  </option>
                ))}
              </select>
              {selectedDocument && (
                <div className="text-xs text-gray-500 mt-1">
                  Selected ID: {selectedDocument}
                </div>
              )}
            </div>

            {documents.length === 0 && (
              <div className="text-sm text-gray-500 bg-gray-50 rounded-lg p-3">
                No completed documents available
              </div>
            )}
          </div>
        </div>

        {/* Chat Area */}
        <div className="lg:col-span-3 bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl flex flex-col h-[600px] border border-white/20">
          {/* Chat Messages */}
          <div className="flex-1 overflow-y-auto p-6 space-y-4">
            {chatHistory.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-full text-gray-400">
                <div className="bg-gradient-to-br from-indigo-100 to-purple-100 p-4 rounded-full mb-4">
                  <MessageSquare className="h-12 w-12 text-indigo-600" />
                </div>
                <p className="text-center font-medium">Start a conversation by asking a question</p>
              </div>
            ) : (
              chatHistory.map((message, index) => (
                <div
                  key={index}
                  className={`flex ${message.role === 'user' ? 'justify-end' : 'justify-start'}`}
                >
                  <div
                    className={`max-w-[80%] rounded-2xl p-4 ${
                      message.role === 'user'
                        ? 'bg-gradient-to-r from-indigo-600 to-purple-600 text-white shadow-md'
                        : 'bg-gray-100 text-gray-900 border border-gray-200'
                    }`}
                  >
                    {message.content}
                  </div>
                </div>
              ))
            )}
            {loading && (
              <div className="flex justify-start">
                <div className="bg-gray-100 rounded-2xl p-4 border border-gray-200">
                  <Loader2 className="h-5 w-5 animate-spin text-indigo-600" />
                </div>
              </div>
            )}
          </div>

          {/* Input Area */}
          <div className="border-t border-gray-200 p-4 bg-white/50">
            <div className="flex space-x-3">
              <textarea
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyPress={handleKeyPress}
                placeholder="Ask a question about your documents..."
                className="flex-1 px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 resize-none bg-white/70 transition-all duration-200"
                rows={2}
                disabled={loading}
              />
              <button
                onClick={handleSendMessage}
                disabled={loading || !query.trim()}
                className="px-6 py-3 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-xl hover:from-indigo-700 hover:to-purple-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-all duration-200 shadow-lg hover:shadow-xl"
              >
                <Send className="h-5 w-5" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
