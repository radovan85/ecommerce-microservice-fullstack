export class ProductCategory {
    private _productCategoryId?: number;
    private _name?: string;

    get productCategoryId(): number | undefined {
        return this._productCategoryId;
    }

    set productCategoryId(value: number | undefined) {
        this._productCategoryId = value;
    }

    get name(): string | undefined {
        return this._name;
    }

    set name(value: string | undefined) {
        this._name = value;
    }
}
