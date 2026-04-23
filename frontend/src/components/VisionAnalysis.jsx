import { useState } from 'react'
import { Upload, Eye, FileText, BarChart3, Loader2, Image as ImageIcon } from 'lucide-react'

export default function VisionAnalysis() {
  const [selectedFile, setSelectedFile] = useState(null)
  const [previewUrl, setPreviewUrl] = useState(null)
  const [analysisType, setAnalysisType] = useState('analyze')
  const [query, setQuery] = useState('')
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const handleFileSelect = (e) => {
    const file = e.target.files[0]
    if (file) {
      setSelectedFile(file)
      setPreviewUrl(URL.createObjectURL(file))
      setResult(null)
      setError(null)
    }
  }

  const handleAnalyze = async () => {
    if (!selectedFile) {
      setError('Please select an image first')
      return
    }

    if (analysisType === 'analyze' && !query.trim()) {
      setError('Please enter a query for analysis')
      return
    }

    setLoading(true)
    setError(null)
    setResult(null)

    const formData = new FormData()
    formData.append('file', selectedFile)
    if (analysisType === 'analyze') {
      formData.append('query', query)
    }

    try {
      let endpoint
      switch (analysisType) {
        case 'analyze':
          endpoint = 'http://localhost:8080/api/vision/analyze'
          break
        case 'extract-text':
          endpoint = 'http://localhost:8080/api/vision/extract-text'
          break
        case 'analyze-chart':
          endpoint = 'http://localhost:8080/api/vision/analyze-chart'
          break
        default:
          endpoint = 'http://localhost:8080/api/vision/analyze'
      }

      const response = await fetch(endpoint, {
        method: 'POST',
        body: formData
      })

      const data = await response.json()

      if (!response.ok) {
        throw new Error(data.error || 'Analysis failed')
      }

      setResult(data)
    } catch (err) {
      setError(err.message || 'Failed to analyze image')
    } finally {
      setLoading(false)
    }
  }

  const handleReset = () => {
    setSelectedFile(null)
    setPreviewUrl(null)
    setQuery('')
    setResult(null)
    setError(null)
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
          Vision Analysis
        </h2>
        <p className="mt-2 text-gray-600">Analyze images using AI vision capabilities</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Left Panel - Upload and Options */}
        <div className="space-y-4">
          {/* File Upload */}
          <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl p-6 border border-white/20">
            <h3 className="font-semibold text-gray-900 mb-4">Upload Image</h3>
            
            <div className="border-2 border-dashed border-gray-300 rounded-xl p-8 text-center hover:border-indigo-400 hover:bg-indigo-50/30 transition-all duration-200">
              <input
                type="file"
                id="image-upload"
                accept="image/*"
                onChange={handleFileSelect}
                className="hidden"
              />
              <label
                htmlFor="image-upload"
                className="cursor-pointer flex flex-col items-center"
              >
                {previewUrl ? (
                  <img
                    src={previewUrl}
                    alt="Preview"
                    className="max-h-64 mb-4 rounded-xl shadow-md"
                  />
                ) : (
                  <>
                    <div className="bg-gradient-to-br from-indigo-100 to-purple-100 p-4 rounded-full mb-3">
                      <ImageIcon className="h-10 w-10 text-indigo-600" />
                    </div>
                    <p className="text-gray-700 font-medium">Click to upload an image</p>
                    <p className="text-sm text-gray-500 mt-1">PNG, JPG up to 50MB</p>
                  </>
                )}
              </label>
            </div>

            {selectedFile && (
              <div className="mt-4 flex items-center justify-between bg-indigo-50 rounded-lg p-3">
                <span className="text-sm text-gray-700 font-medium">
                  {selectedFile.name}
                </span>
                <button
                  onClick={handleReset}
                  className="text-sm text-red-600 hover:text-red-700 font-medium"
                >
                  Remove
                </button>
              </div>
            )}
          </div>

          {/* Analysis Type Selection */}
          <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl p-6 border border-white/20">
            <h3 className="font-semibold text-gray-900 mb-4">Analysis Type</h3>
            
            <div className="space-y-3">
              <label className="flex items-center space-x-3 cursor-pointer p-3 rounded-lg hover:bg-indigo-50/50 transition-colors">
                <input
                  type="radio"
                  value="analyze"
                  checked={analysisType === 'analyze'}
                  onChange={(e) => setAnalysisType(e.target.value)}
                  className="text-indigo-600 focus:ring-indigo-500"
                />
                <div className={`p-2 rounded-lg ${
                  analysisType === 'analyze' ? 'bg-indigo-500' : 'bg-gray-200'
                }`}>
                  <Eye className={`h-5 w-5 ${
                    analysisType === 'analyze' ? 'text-white' : 'text-gray-600'
                  }`} />
                </div>
                <span className="text-gray-700 font-medium">Custom Analysis</span>
              </label>

              <label className="flex items-center space-x-3 cursor-pointer p-3 rounded-lg hover:bg-indigo-50/50 transition-colors">
                <input
                  type="radio"
                  value="extract-text"
                  checked={analysisType === 'extract-text'}
                  onChange={(e) => setAnalysisType(e.target.value)}
                  className="text-indigo-600 focus:ring-indigo-500"
                />
                <div className={`p-2 rounded-lg ${
                  analysisType === 'extract-text' ? 'bg-indigo-500' : 'bg-gray-200'
                }`}>
                  <FileText className={`h-5 w-5 ${
                    analysisType === 'extract-text' ? 'text-white' : 'text-gray-600'
                  }`} />
                </div>
                <span className="text-gray-700 font-medium">Extract Text (OCR)</span>
              </label>

              <label className="flex items-center space-x-3 cursor-pointer p-3 rounded-lg hover:bg-indigo-50/50 transition-colors">
                <input
                  type="radio"
                  value="analyze-chart"
                  checked={analysisType === 'analyze-chart'}
                  onChange={(e) => setAnalysisType(e.target.value)}
                  className="text-indigo-600 focus:ring-indigo-500"
                />
                <div className={`p-2 rounded-lg ${
                  analysisType === 'analyze-chart' ? 'bg-indigo-500' : 'bg-gray-200'
                }`}>
                  <BarChart3 className={`h-5 w-5 ${
                    analysisType === 'analyze-chart' ? 'text-white' : 'text-gray-600'
                  }`} />
                </div>
                <span className="text-gray-700 font-medium">Analyze Chart/Graph</span>
              </label>
            </div>
          </div>

          {/* Query Input for Custom Analysis */}
          {analysisType === 'analyze' && (
            <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl p-6 border border-white/20">
              <h3 className="font-semibold text-gray-900 mb-4">Your Question</h3>
              <textarea
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="What would you like to know about this image?"
                className="w-full px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 resize-none bg-white/50 transition-all duration-200"
                rows={3}
              />
            </div>
          )}

          {/* Analyze Button */}
          <button
            onClick={handleAnalyze}
            disabled={loading || !selectedFile}
            className="w-full px-6 py-4 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-xl hover:from-indigo-700 hover:to-purple-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-all duration-200 shadow-lg hover:shadow-xl flex items-center justify-center space-x-2 font-semibold"
          >
            {loading ? (
              <>
                <Loader2 className="h-5 w-5 animate-spin" />
                <span>Analyzing...</span>
              </>
            ) : (
              <>
                <Eye className="h-5 w-5" />
                <span>Analyze Image</span>
              </>
            )}
          </button>
        </div>

        {/* Right Panel - Results */}
        <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl p-6 border border-white/20">
          <h3 className="font-semibold text-gray-900 mb-4">Analysis Result</h3>
          
          {error && (
            <div className="bg-gradient-to-r from-red-50 to-pink-50 border border-red-200 rounded-xl p-4 mb-4">
              <p className="text-red-800 font-medium">{error}</p>
            </div>
          )}

          {result && (
            <div className="space-y-4">
              <div className="bg-gradient-to-br from-indigo-50 to-purple-50 rounded-xl p-4 border border-indigo-100">
                <h4 className="font-semibold text-gray-900 mb-2">
                  {analysisType === 'analyze' && 'Analysis'}
                  {analysisType === 'extract-text' && 'Extracted Text'}
                  {analysisType === 'analyze-chart' && 'Chart Analysis'}
                </h4>
                <p className="text-gray-700 whitespace-pre-wrap">
                  {result.response || result.text || result.analysis}
                </p>
              </div>

              {result.filename && (
                <div className="text-sm text-gray-600 bg-gray-50 rounded-lg p-3">
                  <span className="font-medium">Analyzed:</span> {result.filename}
                </div>
              )}
            </div>
          )}

          {!result && !error && (
            <div className="text-center text-gray-400 py-12">
              <div className="bg-gradient-to-br from-indigo-100 to-purple-100 p-4 rounded-full w-20 h-20 mx-auto mb-4">
                <Eye className="h-10 w-10 text-indigo-600 mx-auto" />
              </div>
              <p className="font-medium">Upload an image and click analyze to see results</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
