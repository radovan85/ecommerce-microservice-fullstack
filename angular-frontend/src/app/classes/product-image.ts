export class ProductImage {

    private _id?: number;
    private _name?: string;
    private _contentType?: string;
    private _size?: number;
    private _data?: ArrayBuffer;
    private _productId?: number;

    get id(): number | undefined {
        return this._id;
    }

    set id(value: number | undefined) {
        this._id = value;
    }

    get name(): string | undefined {
        return this._name;
    }

    set name(value: string | undefined) {
        this._name = value;
    }

    get contentType(): string | undefined {
        return this._contentType;
    }

    set contentType(value: string | undefined) {
        this._contentType = value;
    }

    get size(): number | undefined {
        return this._size;
    }

    set size(value: number | undefined) {
        this._size = value;
    }

    get data(): ArrayBuffer | undefined {
        return this._data;
    }

    set data(value: ArrayBuffer | undefined) {
        this._data = value;
    }

    get productId(): number | undefined {
        return this._productId;
    }

    set productId(value: number | undefined) {
        this._productId = value;
    }
}
