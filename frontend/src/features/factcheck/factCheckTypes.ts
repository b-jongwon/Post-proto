export type FactCheckStatus =
    | 'PROCESSING'
    | 'COMPLETED'
    | 'FAILED'

export type FactCheckVerdict =
    | 'TRUE'
    | 'MOSTLY_TRUE'
    | 'MIXED'
    | 'MOSTLY_FALSE'
    | 'FALSE'
    | 'UNVERIFIABLE'

export type FactCheckSourceType =
    | 'GOVERNMENT'
    | 'PUBLIC_INSTITUTION'
    | 'ACADEMIC'
    | 'PRIMARY_SOURCE'
    | 'NEWS'
    | 'ENCYCLOPEDIA'
    | 'COMMUNITY'
    | 'BLOG'
    | 'VIDEO'
    | 'OTHER'

export type EvidenceStance =
    | 'SUPPORTS'
    | 'REFUTES'
    | 'CONTEXT'

export interface FactCheckSource {
    sourceId: number
    sourceOrder: number

    title: string
    url: string
    canonicalUrl: string
    domain: string | null

    sourceType: FactCheckSourceType
    snippet: string | null

    publishedAt: string | null
    retrievedAt: string | null
}

export interface FactCheckEvidence {
    evidenceId: number
    evidenceOrder: number

    stance: EvidenceStance
    snippet: string | null
    reasoning: string
    relevanceScore: number

    createdAt: string

    source: FactCheckSource
}

export interface FactCheckClaim {
    claimId: number
    claimOrder: number

    claimText: string
    normalizedClaim: string

    verdict: FactCheckVerdict
    confidenceScore: number

    explanation: string
    createdAt: string

    evidences: FactCheckEvidence[]
}

export interface FactCheckResponse {
    analysisId: number
    postId: number
    runNumber: number

    status: FactCheckStatus
    verdict: FactCheckVerdict | null

    credibilityScore: number | null
    confidenceScore: number | null

    summary: string | null
    explanation: string | null

    model: string | null
    interactionId: string | null
    promptVersion: string
    schemaVersion: string

    postTitleSnapshot: string
    postContentSnapshot: string
    postContentHash: string

    isStale: boolean
    errorMessage: string | null

    requestedByUserId: number
    requestedByNickname: string

    createdAt: string
    updatedAt: string
    completedAt: string | null

    sources: FactCheckSource[]
    claims: FactCheckClaim[]

    disclaimer: string
}

export interface FactCheckHistoryItem {
    analysisId: number
    postId: number
    runNumber: number

    status: FactCheckStatus
    verdict: FactCheckVerdict | null

    credibilityScore: number | null
    confidenceScore: number | null

    summary: string | null
    model: string | null

    promptVersion: string
    schemaVersion: string

    isStale: boolean
    isSelected: boolean

    errorMessage: string | null

    requestedByUserId: number
    requestedByNickname: string

    createdAt: string
    completedAt: string | null
}