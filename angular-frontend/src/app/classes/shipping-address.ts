export class ShippingAddress {
    private _shippingAddressId?: number;
    private _address?: string;
    private _city?: string;
    private _state?: string;
    private _country?: string;
    private _postcode?: string;
    private _customerId?: number;

    get shippingAddressId(): number | undefined {
        return this._shippingAddressId;
    }

    set shippingAddressId(value: number | undefined) {
        this._shippingAddressId = value;
    }

    get address(): string | undefined {
        return this._address;
    }

    set address(value: string | undefined) {
        this._address = value;
    }

    get city(): string | undefined {
        return this._city;
    }

    set city(value: string | undefined) {
        this._city = value;
    }

    get state(): string | undefined {
        return this._state;
    }

    set state(value: string | undefined) {
        this._state = value;
    }

    get country(): string | undefined {
        return this._country;
    }

    set country(value: string | undefined) {
        this._country = value;
    }

    get postcode(): string | undefined {
        return this._postcode;
    }

    set postcode(value: string | undefined) {
        this._postcode = value;
    }

    get customerId(): number | undefined {
        return this._customerId;
    }

    set customerId(value: number | undefined) {
        this._customerId = value;
    }
}
