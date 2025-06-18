export class User {
    private _id?: number;
    private _firstName?: string;
    private _lastName?: string;
    private _email?: string;
    private _password?: string;
    private _enabled?: number;
    private _rolesIds?: number[];
    private _authToken?: string;

    get id(): number | undefined {
        return this._id;
    }

    set id(value: number | undefined) {
        this._id = value;
    }

    get firstName(): string | undefined {
        return this._firstName;
    }

    set firstName(value: string | undefined) {
        this._firstName = value;
    }

    get lastName(): string | undefined {
        return this._lastName;
    }

    set lastName(value: string | undefined) {
        this._lastName = value;
    }

    get email(): string | undefined {
        return this._email;
    }

    set email(value: string | undefined) {
        this._email = value;
    }

    get password(): string | undefined {
        return this._password;
    }

    set password(value: string | undefined) {
        this._password = value;
    }

    get enabled(): number | undefined {
        return this._enabled;
    }

    set enabled(value: number | undefined) {
        this._enabled = value;
    }

    get rolesIds(): number[] | undefined {
        return this._rolesIds;
    }

    set rolesIds(value: number[] | undefined) {
        this._rolesIds = value;
    }

    get authToken(): string | undefined {
        return this._authToken;
    }

    set authToken(value: string | undefined) {
        this._authToken = value;
    }
}