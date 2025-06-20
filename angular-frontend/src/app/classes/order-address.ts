export class OrderAddress {
    private _orderAddressId?: number;
    private _address?: string;
    private _city?: string;
    private _state?: string;
    private _country?: string;
    private _postcode?: string;
    private _orderId?: number;

    get orderAddressId(): number | undefined {
        return this._orderAddressId;
    }

    set orderAddressId(value: number | undefined) {
        this._orderAddressId = value;
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

    get orderId(): number | undefined {
        return this._orderId;
    }

    set orderId(value: number | undefined) {
        this._orderId = value;
    }
}

