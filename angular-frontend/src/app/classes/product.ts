export class Product {
    private _productId?: number;
    private _productDescription?: string;
    private _productBrand?: string;
    private _productModel?: string;
    private _productName?: string;
    private _productPrice?: number;
    private _unitStock?: number;
    private _discount?: number;
    private _imageId?: number;
    private _productCategoryId?: number;

    get productId(): number | undefined {
        return this._productId;
    }

    set productId(value: number | undefined) {
        this._productId = value;
    }

    get productDescription(): string | undefined {
        return this._productDescription;
    }

    set productDescription(value: string | undefined) {
        this._productDescription = value;
    }

    get productBrand(): string | undefined {
        return this._productBrand;
    }

    set productBrand(value: string | undefined) {
        this._productBrand = value;
    }

    get productModel(): string | undefined {
        return this._productModel;
    }

    set productModel(value: string | undefined) {
        this._productModel = value;
    }

    get productName(): string | undefined {
        return this._productName;
    }

    set productName(value: string | undefined) {
        this._productName = value;
    }

    get productPrice(): number | undefined {
        return this._productPrice;
    }

    set productPrice(value: number | undefined) {
        this._productPrice = value;
    }

    get unitStock(): number | undefined {
        return this._unitStock;
    }

    set unitStock(value: number | undefined) {
        this._unitStock = value;
    }

    get discount(): number | undefined {
        return this._discount;
    }

    set discount(value: number | undefined) {
        this._discount = value;
    }

    get imageId(): number | undefined {
        return this._imageId;
    }

    set imageId(value: number | undefined) {
        this._imageId = value;
    }

    get productCategoryId(): number | undefined {
        return this._productCategoryId;
    }

    set productCategoryId(value: number | undefined) {
        this._productCategoryId = value;
    }
}
