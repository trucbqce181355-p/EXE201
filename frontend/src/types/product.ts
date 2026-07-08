export interface Product {
    id: number;
    name: string;
    sku: string;
    price: number;
    available: boolean;
    status: "ACTIVE" | "INACTIVE" | "DRAFT" | "ARCHIVED";
    categoryId: number;
    categoryName: string;
    thumbnailUrl: string | null;
    updatedAt: string;
    preparationTime?: number;
    description?: string;
}

export interface ProductResponse {
    data: Product[];
    total: number;
    page: number;
    limit: number;
    totalPages: number;
}