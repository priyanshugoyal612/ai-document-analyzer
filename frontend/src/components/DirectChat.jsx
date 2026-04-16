import { useState } from 'react'
import { Send, Loader2, MessageSquare, Code, FileText } from 'lucide-react'

export default function DirectChat() {
  const [chatType, setChatType] = useState('direct') // 'direct' or 'json'
  const [query, setQuery] = useState('')
  const [context, setContext] = useState('')
  const [chatHistory, setChatHistory] = useState([])
  const [loading, setLoading] = useState(false)

  const handleSendMessage = async () => {
    if (!query.trim()) return

    setLoading(true)
    const userMessage = { role: 'user', content: query }
    setChatHistory([...chatHistory, userMessage])

    try {
      let endpoint, body
      if (chatType === 'direct') {
        endpoint = 'http://localhost:8080/api/openai/chat/direct'
        body = JSON.stringify({ query, context })
      } else {
        endpoint = 'http://localhost:8080/api/openai/chat/json'
        body = JSON.stringify({ query, jsonContext: context })
      }

      const response = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body
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
        <h2 className="text-3xl font-bold text-gray-900">Direct Chat</h2>
        <p className="mt-2 text-gray-600">Chat with OpenAI using custom context or JSON data</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Sidebar */}
        <div className="lg:col-span-1 space-y-4">
          <div className="bg-white rounded-lg shadow p-4 space-y-4">
            <h3 className="font-medium text-gray-900">Settings</h3>
            
            {/* Chat Type Selection */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Context Type
              </label>
              <div className="grid grid-cols-2 gap-2">
                <button
                  onClick={() => setChatType('direct')}
                  className={`p-3 border-2 rounded-lg text-left ${
                    chatType === 'direct'
                      ? 'border-blue-500 bg-blue-50'
                      : 'border-gray-300 hover:border-gray-400'
                  }`}
                >
                  <div className="flex items-center space-x-2">
                    <FileText className="h-4 w-4 text-blue-600" />
                    <span className="text-sm font-medium">Text</span>
                  </div>
                </button>
                <button
                  onClick={() => setChatType('json')}
                  className={`p-3 border-2 rounded-lg text-left ${
                    chatType === 'json'
                      ? 'border-blue-500 bg-blue-50'
                      : 'border-gray-300 hover:border-gray-400'
                  }`}
                >
                  <div className="flex items-center space-x-2">
                    <Code className="h-4 w-4 text-purple-600" />
                    <span className="text-sm font-medium">JSON</span>
                  </div>
                </button>
              </div>
            </div>

            {/* Context Input */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                {chatType === 'direct' ? 'Context' : 'JSON Context'}
              </label>
              <textarea
                value={context}
                onChange={(e) => setContext(e.target.value)}
                placeholder={chatType === 'direct' 
                  ? 'Enter your context here...' 
                  : 'Enter JSON data here...'}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-none"
                rows={8}
              />
            </div>

            <div className="text-xs text-gray-500">
              <p className="mb-1">Example for Text:</p>
              <code className="bg-gray-100 p-1 rounded">Java is a programming language</code>
              <p className="mt-2 mb-1">Example for JSON:</p>
              <code className="bg-gray-100 p-1 rounded">{`{"key": "value"}`}</code>
            </div>
          </div>
        </div>

        {/* Chat Area */}
        <div className="lg:col-span-2 bg-white rounded-lg shadow flex flex-col h-[600px]">
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
                placeholder="Ask a question..."
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
