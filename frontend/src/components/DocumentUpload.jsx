import { useState } from 'react'
import { Upload, FileText, CheckCircle, AlertCircle, Loader2 } from 'lucide-react'

export default function DocumentUpload() {
  const [file, setFile] = useState(null)
  const [uploadType, setUploadType] = useState('pageindex')
  const [uploading, setUploading] = useState(false)
  const [uploadStatus, setUploadStatus] = useState(null)
  const [documentId, setDocumentId] = useState(null)

  const handleFileChange = (e) => {
    setFile(e.target.files[0])
    setUploadStatus(null)
    setDocumentId(null)
  }

  const handleUpload = async () => {
    if (!file) return

    setUploading(true)
    setUploadStatus(null)

    const formData = new FormData()
    formData.append('file', file)

    try {
      const endpoint = uploadType === 'pageindex' 
        ? 'http://localhost:8080/api/documents/upload'
        : 'http://localhost:8080/api/openai/documents/upload'

      const response = await fetch(endpoint, {
        method: 'POST',
        body: formData
      })

      const data = await response.json()

      if (response.ok) {
        setUploadStatus({ success: true, message: data.message || 'Document uploaded successfully' })
        setDocumentId(data.id)
        setFile(null)
      } else {
        setUploadStatus({ success: false, message: data.message || 'Upload failed' })
      }
    } catch (error) {
      setUploadStatus({ success: false, message: 'Error uploading document' })
    } finally {
      setUploading(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
          Upload Document
        </h2>
        <p className="mt-2 text-gray-600">Upload documents for indexing with PageIndex or OpenAI</p>
      </div>

      <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-xl p-8 space-y-6 border border-white/20">
        {/* Upload Type Selection */}
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-3">
            Indexing Method
          </label>
          <div className="grid grid-cols-2 gap-4">
            <button
              onClick={() => setUploadType('pageindex')}
              className={`p-5 border-2 rounded-xl text-left transition-all duration-200 ${
                uploadType === 'pageindex'
                  ? 'border-indigo-500 bg-gradient-to-br from-indigo-50 to-purple-50 shadow-md'
                  : 'border-gray-200 hover:border-indigo-300 hover:bg-indigo-50/50'
              }`}
            >
              <div className="flex items-center space-x-3">
                <div className={`p-2 rounded-lg ${
                  uploadType === 'pageindex' ? 'bg-indigo-500' : 'bg-gray-200'
                }`}>
                  <FileText className={`h-5 w-5 ${
                    uploadType === 'pageindex' ? 'text-white' : 'text-gray-600'
                  }`} />
                </div>
                <div>
                  <div className="font-semibold text-gray-900">PageIndex</div>
                  <div className="text-sm text-gray-500">Professional RAG (98.7% accuracy)</div>
                </div>
              </div>
            </button>
            <button
              onClick={() => setUploadType('openai')}
              className={`p-5 border-2 rounded-xl text-left transition-all duration-200 ${
                uploadType === 'openai'
                  ? 'border-purple-500 bg-gradient-to-br from-purple-50 to-pink-50 shadow-md'
                  : 'border-gray-200 hover:border-purple-300 hover:bg-purple-50/50'
              }`}
            >
              <div className="flex items-center space-x-3">
                <div className={`p-2 rounded-lg ${
                  uploadType === 'openai' ? 'bg-purple-500' : 'bg-gray-200'
                }`}>
                  <FileText className={`h-5 w-5 ${
                    uploadType === 'openai' ? 'text-white' : 'text-gray-600'
                  }`} />
                </div>
                <div>
                  <div className="font-semibold text-gray-900">OpenAI</div>
                  <div className="text-sm text-gray-500">Custom keyword indexing</div>
                </div>
              </div>
            </button>
          </div>
        </div>

        {/* File Upload */}
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-3">
            Select File
          </label>
          <div className="border-2 border-dashed border-gray-300 rounded-xl p-8 text-center hover:border-indigo-400 hover:bg-indigo-50/30 transition-all duration-200">
            <input
              type="file"
              onChange={handleFileChange}
              accept=".pdf,.docx,.txt"
              className="hidden"
              id="file-upload"
            />
            <label htmlFor="file-upload" className="cursor-pointer">
              <div className={`p-4 rounded-full mx-auto w-16 h-16 flex items-center justify-center mb-4 ${
                file ? 'bg-gradient-to-br from-indigo-500 to-purple-500' : 'bg-gray-100'
              }`}>
                <Upload className={`h-8 w-8 ${
                  file ? 'text-white' : 'text-gray-400'
                }`} />
              </div>
              <p className="text-sm font-medium text-gray-700">
                Click to upload or drag and drop
              </p>
              <p className="text-xs text-gray-500 mt-1">
                PDF, DOCX, TXT (Max 50MB)
              </p>
            </label>
            {file && (
              <div className="mt-4 flex items-center justify-center space-x-2 text-sm text-gray-700 bg-indigo-50 rounded-lg py-2 px-4">
                <FileText className="h-4 w-4 text-indigo-600" />
                <span className="font-medium">{file.name}</span>
                <span className="text-gray-500">({(file.size / 1024).toFixed(2)} KB)</span>
              </div>
            )}
          </div>
        </div>

        {/* Upload Button */}
        <button
          onClick={handleUpload}
          disabled={!file || uploading}
          className={`w-full py-4 px-4 rounded-xl font-semibold text-white transition-all duration-200 shadow-lg ${
            !file || uploading
              ? 'bg-gray-300 cursor-not-allowed'
              : 'bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 hover:shadow-xl'
          }`}
        >
          {uploading ? (
            <div className="flex items-center justify-center space-x-2">
              <Loader2 className="h-5 w-5 animate-spin" />
              <span>Uploading...</span>
            </div>
          ) : (
            'Upload Document'
          )}
        </button>

        {/* Status Message */}
        {uploadStatus && (
          <div className={`flex items-center space-x-2 p-4 rounded-xl ${
            uploadStatus.success
              ? 'bg-gradient-to-r from-green-50 to-emerald-50 text-green-800 border border-green-200'
              : 'bg-gradient-to-r from-red-50 to-pink-50 text-red-800 border border-red-200'
          }`}>
            {uploadStatus.success ? (
              <CheckCircle className="h-5 w-5" />
            ) : (
              <AlertCircle className="h-5 w-5" />
            )}
            <span className="font-medium">{uploadStatus.message}</span>
            {documentId && (
              <span className="ml-2 text-sm text-gray-600">(Document ID: {documentId})</span>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
