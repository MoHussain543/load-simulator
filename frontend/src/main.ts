import './style.css'

const BACKEND_URL = 'http://localhost:8080'

interface LoadTestRequest {
  url: string
  method: string
  virtualUsers: number
  durationSeconds: number
}

interface LoadTestResult {
  totalRequests: number
  successfulRequests: number
  failedRequests: number
  averageResponseTimeMs: number
  minResponseTimeMs: number
  maxResponseTimeMs: number
  p95ResponseTimeMs: number
  requestsPerSecond: number
  errorRate: number
}

interface ApiErrorResponse {
  message: string
  errors?: Record<string, string>
}

// ── DOM refs ──────────────────────────────────────────────────────────────────

const form          = document.getElementById('load-test-form')       as HTMLFormElement
const runBtn        = document.getElementById('run-btn')               as HTMLButtonElement
const urlInput      = document.getElementById('url')                   as HTMLInputElement
const usersInput    = document.getElementById('virtualUsers')          as HTMLInputElement
const durationInput = document.getElementById('durationSeconds')       as HTMLInputElement

const statusArea    = document.getElementById('status-area')           as HTMLDivElement
const loadingPrimary   = document.getElementById('loading-primary')    as HTMLParagraphElement
const loadingSecondary = document.getElementById('loading-secondary')  as HTMLParagraphElement

const errorArea     = document.getElementById('error-area')            as HTMLDivElement
const errorTitle    = document.getElementById('error-title')           as HTMLParagraphElement
const errorDetail   = document.getElementById('error-detail')          as HTMLParagraphElement

const resultsCard   = document.getElementById('results-card')          as HTMLElement
const resultsMeta   = document.getElementById('results-meta')          as HTMLDivElement

const mTotal      = document.getElementById('m-total')       as HTMLSpanElement
const mSuccess    = document.getElementById('m-success')     as HTMLSpanElement
const mFailed     = document.getElementById('m-failed')      as HTMLSpanElement
const mFailedCard = document.getElementById('m-failed-card') as HTMLDivElement
const mAvg        = document.getElementById('m-avg')         as HTMLSpanElement
const mMin        = document.getElementById('m-min')         as HTMLSpanElement
const mMax        = document.getElementById('m-max')         as HTMLSpanElement
const mP95        = document.getElementById('m-p95')         as HTMLSpanElement
const mRps        = document.getElementById('m-rps')         as HTMLSpanElement
const mErrorRate      = document.getElementById('m-error-rate')       as HTMLSpanElement
const mErrorRateCard  = document.getElementById('m-error-rate-card')  as HTMLDivElement

// ── Helpers ───────────────────────────────────────────────────────────────────

function fmt(n: number, decimals = 2): string {
  return n.toLocaleString('en-US', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  })
}

function fmtInt(n: number): string {
  return n.toLocaleString('en-US')
}

function setRunning(running: boolean): void {
  urlInput.disabled      = running
  usersInput.disabled    = running
  runBtn.disabled        = running
}

function showStatus(): void {
  statusArea.classList.remove('hidden')
  errorArea.classList.add('hidden')
  resultsCard.classList.add('hidden')
}

function hideStatus(): void {
  statusArea.classList.add('hidden')
}

function showError(title: string, detail: string): void {
  errorTitle.textContent  = title
  errorDetail.textContent = detail
  errorArea.classList.remove('hidden')
  statusArea.classList.add('hidden')
}

function showResults(result: LoadTestResult, req: LoadTestRequest): void {
  resultsMeta.innerHTML =
    `url: ${req.url}\n` +
    `method: ${req.method}  ·  ` +
    `virtual users: ${req.virtualUsers}  ·  ` +
    `duration: ${req.durationSeconds}s`

  mTotal.textContent   = fmtInt(result.totalRequests)
  mSuccess.textContent = fmtInt(result.successfulRequests)
  mFailed.textContent  = fmtInt(result.failedRequests)

  mAvg.textContent = fmt(result.averageResponseTimeMs)
  mMin.textContent = fmt(result.minResponseTimeMs)
  mMax.textContent = fmt(result.maxResponseTimeMs)
  mP95.textContent = fmt(result.p95ResponseTimeMs)

  mRps.textContent       = fmt(result.requestsPerSecond, 1)
  mErrorRate.textContent = fmt(result.errorRate * 100, 2)

  // Highlight failed requests and error rate in red if non-zero
  const hasFailed = result.failedRequests > 0
  mFailedCard.className   = 'metric-card' + (hasFailed ? ' metric-card--error' : ' metric-card--success')
  mErrorRateCard.className = 'metric-card' + (hasFailed ? ' metric-card--error' : ' metric-card--accent')

  resultsCard.classList.remove('hidden')
  statusArea.classList.add('hidden')
}

function buildErrorDetail(body: ApiErrorResponse): string {
  if (body.errors && Object.keys(body.errors).length > 0) {
    return Object.entries(body.errors)
      .map(([field, msg]) => `${field}: ${msg}`)
      .join('\n')
  }
  return body.message
}

// ── Validation ────────────────────────────────────────────────────────────────

function clientValidate(): string | null {
  const url      = urlInput.value.trim()
  const users    = parseInt(usersInput.value, 10)
  const duration = parseInt(durationInput.value, 10)

  if (!url) return 'Target URL is required.'

  const localhostPattern = /^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?(\/.*)?$/
  if (!localhostPattern.test(url)) {
    return 'URL must start with http://localhost or http://127.0.0.1.'
  }

  if (isNaN(users) || users < 1 || users > 1000) {
    return 'Virtual users must be between 1 and 1,000.'
  }

  if (isNaN(duration) || duration < 1 || duration > 60) {
    return 'Duration must be between 1 and 60 seconds.'
  }

  return null
}

// ── Submit handler ────────────────────────────────────────────────────────────

form.addEventListener('submit', async (e) => {
  e.preventDefault()

  urlInput.classList.remove('input--error')

  const validationError = clientValidate()
  if (validationError) {
    showError('Invalid input', validationError)
    return
  }

  const request: LoadTestRequest = {
    url:             urlInput.value.trim(),
    method:          'GET',
    virtualUsers:    parseInt(usersInput.value, 10),
    durationSeconds: parseInt(durationInput.value, 10),
  }

  setRunning(true)
  showStatus()

  loadingPrimary.textContent = 'Running load test…'
  loadingSecondary.textContent =
    `${request.virtualUsers} virtual user${request.virtualUsers !== 1 ? 's' : ''} · ` +
    `${request.durationSeconds}s duration — backend runs the full duration before returning`

  try {
    const response = await fetch(`${BACKEND_URL}/api/load-test`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    })

    if (response.ok) {
      const result = (await response.json()) as LoadTestResult
      showResults(result, request)
    } else {
      let title   = `Error ${response.status}`
      let detail  = `The backend returned HTTP ${response.status}.`

      try {
        const body = (await response.json()) as ApiErrorResponse
        detail = buildErrorDetail(body)
        if (response.status === 400) title = 'Validation failed'
        else if (response.status === 500) title = 'Backend error'
      } catch {
        // body wasn't JSON — keep the generic message
      }

      showError(title, detail)
    }
  } catch (err) {
    const isNetworkError =
      err instanceof TypeError && err.message.toLowerCase().includes('fetch')

    if (isNetworkError) {
      showError(
        'Cannot reach the backend',
        'Make sure the Spring Boot server is running on http://localhost:8080.\n\n' +
        'From the backend/ directory: ./mvnw spring-boot:run',
      )
    } else {
      showError('Unexpected error', err instanceof Error ? err.message : String(err))
    }
  } finally {
    setRunning(false)
    hideStatus()
  }
})
