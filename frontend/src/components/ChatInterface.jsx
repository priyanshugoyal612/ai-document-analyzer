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
        <h2 className="text-3xl font-bold text-gray-900">Chat with Documents</h2>
        <p className="mt-2 text-gray-600">Ask questions about your documents using PageIndex or OpenAI</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        {/* Sidebar */}
        <div className="lg:col-span-1 space-y-4">
          <div className="bg-white rounded-lg shadow p-4 space-y-4">
            <h3 className="font-medium text-gray-900">Settings</h3>
            
            {/* Chat Type Selection */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Chat Method
              </label>
              <select
                value={chatType}
                onChange={(e) => setChatType(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="openai">OpenAI</option>
                <option value="pageindex">PageIndex</option>
              </select>
            </div>

            {/* Document Selection */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Document
              </label>
              <select
                value={selectedDocument || ''}
                onChange={(e) => {
                  console.log('Selected document:', e.target.value)
                  setSelectedDocument(e.target.value)
                }}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="">All Documents</option>
                {documents.map((doc) => (
                  <option key={doc.id} value={String(doc.id)}>
                    {doc.originalFilename}
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
              <div className="text-sm text-gray-500">
                No completed documents available
              </div>
            )}
          </div>
        </div>

        {/* Chat Area */}
        <div className="lg:col-span-3 bg-white rounded-lg shadow flex flex-col h-[600px]">
          {/* Chat Messages */}
          <div className="flex-1 overflow-y-auto p-4 space-y-4">
            {chatHistory.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-full text-gray-400">
                <MessageSquare className="h-16 w-16 mb-4" />
                <p className="text-center">Start a conversation by asking a question</p>
              </div>
            ) : (
              chatHistory.map((message, index) => (
                <div
                  key={index}
                  className={`flex ${message.role === 'user' ? 'justify-end' : 'justify-start'}`}
                >
                  <div
                    className={`max-w-[80%] rounded-lg p-4 ${
                      message.role === 'user'
                        ? 'bg-blue-600 text-white'
                        : 'bg-gray-100 text-gray-900'
                    }`}
                  >
                    {message.content}
                  </div>
                </div>
              ))
            )}
            {loading && (
              <div className="flex justify-start">
                <div className="bg-gray-100 rounded-lg p-4">
                  <Loader2 className="h-5 w-5 animate-spin text-gray-600" />
                </div>
              </div>
            )}
          </div>

          {/* Input Area */}
          <div className="border-t border-gray-200 p-4">
            <div className="flex space-x-3">
              <textarea
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyPress={handleKeyPress}
                placeholder="Ask a question about your documents..."
                className="flex-1 px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-none"
                rows={2}
                disabled={loading}
              />
              <button
                onClick={handleSendMessage}
                disabled={loading || !query.trim()}
                className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
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
