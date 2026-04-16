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
        <h2 className="text-3xl font-bold text-gray-900">Upload Document</h2>
        <p className="mt-2 text-gray-600">Upload documents for indexing with PageIndex or OpenAI</p>
      </div>

      <div className="bg-white rounded-lg shadow p-6 space-y-6">
        {/* Upload Type Selection */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Indexing Method
          </label>
          <div className="grid grid-cols-2 gap-4">
            <button
              onClick={() => setUploadType('pageindex')}
              className={`p-4 border-2 rounded-lg text-left ${
                uploadType === 'pageindex'
                  ? 'border-blue-500 bg-blue-50'
                  : 'border-gray-300 hover:border-gray-400'
              }`}
            >
              <div className="flex items-center space-x-3">
                <FileText className="h-5 w-5 text-blue-600" />
                <div>
                  <div className="font-medium text-gray-900">PageIndex</div>
                  <div className="text-sm text-gray-500">Professional RAG (98.7% accuracy)</div>
                </div>
              </div>
            </button>
            <button
              onClick={() => setUploadType('openai')}
              className={`p-4 border-2 rounded-lg text-left ${
                uploadType === 'openai'
                  ? 'border-blue-500 bg-blue-50'
                  : 'border-gray-300 hover:border-gray-400'
              }`}
            >
              <div className="flex items-center space-x-3">
                <FileText className="h-5 w-5 text-purple-600" />
                <div>
                  <div className="font-medium text-gray-900">OpenAI</div>
                  <div className="text-sm text-gray-500">Custom keyword indexing</div>
                </div>
              </div>
            </button>
          </div>
        </div>

        {/* File Upload */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Select File
          </label>
          <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-gray-400 transition-colors">
            <input
              type="file"
              onChange={handleFileChange}
              accept=".pdf,.docx,.txt"
              className="hidden"
              id="file-upload"
            />
            <label htmlFor="file-upload" className="cursor-pointer">
              <Upload className="h-12 w-12 mx-auto text-gray-400 mb-3" />
              <p className="text-sm text-gray-600">
                Click to upload or drag and drop
              </p>
              <p className="text-xs text-gray-500 mt-1">
                PDF, DOCX, TXT (Max 50MB)
              </p>
            </label>
            {file && (
              <div className="mt-4 flex items-center justify-center space-x-2 text-sm text-gray-700">
                <FileText className="h-4 w-4" />
                <span>{file.name}</span>
                <span className="text-gray-500">({(file.size / 1024).toFixed(2)} KB)</span>
              </div>
            )}
          </div>
        </div>

        {/* Upload Button */}
        <button
          onClick={handleUpload}
          disabled={!file || uploading}
          className={`w-full py-3 px-4 rounded-lg font-medium text-white transition-colors ${
            !file || uploading
              ? 'bg-gray-300 cursor-not-allowed'
              : 'bg-blue-600 hover:bg-blue-700'
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
          <div className={`flex items-center space-x-2 p-4 rounded-lg ${
            uploadStatus.success
              ? 'bg-green-50 text-green-800'
              : 'bg-red-50 text-red-800'
          }`}>
            {uploadStatus.success ? (
              <CheckCircle className="h-5 w-5" />
            ) : (
              <AlertCircle className="h-5 w-5" />
            )}
            <span>{uploadStatus.message}</span>
            {documentId && (
              <span className="ml-2 text-sm">(Document ID: {documentId})</span>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
