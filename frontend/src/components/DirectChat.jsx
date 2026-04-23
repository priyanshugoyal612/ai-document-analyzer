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
        <h2 className="text-3xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
          Direct Chat
        </h2>
        <p className="mt-2 text-gray-600">Chat with OpenAI using custom context or JSON data</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Sidebar */}
        <div className="lg:col-span-1 space-y-4">
          <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl p-6 space-y-5 border border-white/20">
            <h3 className="font-semibold text-gray-900">Settings</h3>
            
            {/* Chat Type Selection */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Context Type
              </label>
              <div className="grid grid-cols-2 gap-2">
                <button
                  onClick={() => setChatType('direct')}
                  className={`p-4 border-2 rounded-xl text-left transition-all duration-200 ${
                    chatType === 'direct'
                      ? 'border-indigo-500 bg-gradient-to-br from-indigo-50 to-purple-50 shadow-md'
                      : 'border-gray-200 hover:border-indigo-300 hover:bg-indigo-50/50'
                  }`}
                >
                  <div className="flex items-center space-x-2">
                    <div className={`p-2 rounded-lg ${
                      chatType === 'direct' ? 'bg-indigo-500' : 'bg-gray-200'
                    }`}>
                      <FileText className={`h-4 w-4 ${
                        chatType === 'direct' ? 'text-white' : 'text-gray-600'
                      }`} />
                    </div>
                    <span className="text-sm font-semibold">Text</span>
                  </div>
                </button>
                <button
                  onClick={() => setChatType('json')}
                  className={`p-4 border-2 rounded-xl text-left transition-all duration-200 ${
                    chatType === 'json'
                      ? 'border-purple-500 bg-gradient-to-br from-purple-50 to-pink-50 shadow-md'
                      : 'border-gray-200 hover:border-purple-300 hover:bg-purple-50/50'
                  }`}
                >
                  <div className="flex items-center space-x-2">
                    <div className={`p-2 rounded-lg ${
                      chatType === 'json' ? 'bg-purple-500' : 'bg-gray-200'
                    }`}>
                      <Code className={`h-4 w-4 ${
                        chatType === 'json' ? 'text-white' : 'text-gray-600'
                      }`} />
                    </div>
                    <span className="text-sm font-semibold">JSON</span>
                  </div>
                </button>
              </div>
            </div>

            {/* Context Input */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                {chatType === 'direct' ? 'Context' : 'JSON Context'}
              </label>
              <textarea
                value={context}
                onChange={(e) => setContext(e.target.value)}
                placeholder={chatType === 'direct' 
                  ? 'Enter your context here...' 
                  : 'Enter JSON data here...'}
                className="w-full px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 resize-none bg-white/50 transition-all duration-200"
                rows={8}
              />
            </div>

            <div className="text-xs text-gray-500 bg-gray-50 rounded-lg p-3">
              <p className="mb-1 font-medium">Example for Text:</p>
              <code className="bg-white p-1 rounded border border-gray-200">Java is a programming language</code>
              <p className="mt-2 mb-1 font-medium">Example for JSON:</p>
              <code className="bg-white p-1 rounded border border-gray-200">{`{"key": "value"}`}</code>
            </div>
          </div>
        </div>

        {/* Chat Area */}
        <div className="lg:col-span-2 bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl flex flex-col h-[600px] border border-white/20">
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
                placeholder="Ask a question..."
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
